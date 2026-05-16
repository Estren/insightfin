package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.model.EmailVerificationPurpose;
import com.orizon.coreapi.domain.model.EmailVerificationToken;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.port.in.ConfirmEmailChangeUseCase;
import com.orizon.coreapi.domain.port.in.ConfirmEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.in.RequestEmailChangeUseCase;
import com.orizon.coreapi.domain.port.in.RequestEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.in.ResendEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.out.EmailSender;
import com.orizon.coreapi.domain.port.out.EmailVerificationTokenRepository;
import com.orizon.coreapi.domain.port.out.UserRepository;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class EmailVerificationService implements
        RequestEmailVerificationUseCase,
        ResendEmailVerificationUseCase,
        ConfirmEmailVerificationUseCase,
        RequestEmailChangeUseCase,
        ConfirmEmailChangeUseCase {

    private static final Logger LOG = Logger.getLogger(EmailVerificationService.class);
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final int registrationTtlHours;
    private final int pinMaxAttempts;
    private final String frontendBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(UserRepository userRepository,
                                    EmailVerificationTokenRepository tokenRepository,
                                    EmailSender emailSender,
                                    int registrationTtlHours,
                                    int pinMaxAttempts,
                                    String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.registrationTtlHours = registrationTtlHours;
        this.pinMaxAttempts = pinMaxAttempts;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    // --- RequestEmailVerificationUseCase ---
    @Override
    public void execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));
        if (user.isEmailVerified()) {
            LOG.debugf("User %s already verified — skipping verification request", userId);
            return;
        }
        sendVerification(user);
    }

    // --- ResendEmailVerificationUseCase ---
    @Override
    public void execute(String email) {
        String normalized = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        Optional<User> maybeUser = userRepository.findByEmail(normalized);
        if (maybeUser.isEmpty()) {
            // Silent no-op to avoid email enumeration.
            LOG.debugf("Resend verification requested for unknown email: %s", normalized);
            return;
        }
        User user = maybeUser.get();
        if (user.isEmailVerified()) {
            LOG.debugf("Resend verification ignored — user %s already verified", user.getId());
            return;
        }
        sendVerification(user);
    }

    // --- ConfirmEmailVerificationUseCase (link path) ---
    @Override
    public void confirmByLink(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException("Invalid or expired verification token");
        }
        String tokenHash = sha256(rawToken);
        EmailVerificationToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invalid or expired verification token"));
        verifyTokenAndMarkUser(token);
    }

    // --- ConfirmEmailVerificationUseCase (PIN path) ---
    @Override
    public void confirmByPin(String email, String pin) {
        String normalized = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        if (pin == null || pin.isBlank()) {
            throw new DomainException("Invalid or expired verification PIN");
        }
        Optional<User> maybeUser = userRepository.findByEmail(normalized);
        if (maybeUser.isEmpty()) {
            // Silent generic error to avoid enumeration.
            throw new DomainException("Invalid or expired verification PIN");
        }
        User user = maybeUser.get();
        Optional<EmailVerificationToken> maybeToken = tokenRepository.findActiveByUserAndPurpose(
                user.getId(), EmailVerificationPurpose.REGISTRATION);
        if (maybeToken.isEmpty()) {
            throw new DomainException("Invalid or expired verification PIN");
        }
        EmailVerificationToken token = maybeToken.get();
        if (token.isExpired() || token.isUsed()) {
            throw new DomainException("Invalid or expired verification PIN");
        }

        token.setPinAttempts(token.getPinAttempts() + 1);
        boolean match = constantTimeEquals(sha256(pin), token.getPinHash());

        if (!match) {
            if (token.getPinAttempts() >= pinMaxAttempts) {
                token.setUsedAt(LocalDateTime.now());
            }
            tokenRepository.save(token);
            throw new DomainException("Invalid or expired verification PIN");
        }

        // Match — consume token and verify user.
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        markUserVerified(user);
    }

    // --- RequestEmailChangeUseCase ---
    @Override
    public void execute(UUID userId, String newEmail) {
        String normalized = newEmail == null ? null : newEmail.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));
        if (normalized == null || normalized.equals(user.getEmail())) {
            throw new DomainException("New email must be different from the current email");
        }
        if (userRepository.existsByEmail(normalized)) {
            throw new DuplicateResourceException("Email already registered: " + normalized);
        }
        tokenRepository.invalidateActiveByUserAndPurpose(userId, EmailVerificationPurpose.EMAIL_CHANGE);

        String rawToken = generateToken();
        String pin = generatePin();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setTargetEmail(normalized);
        token.setTokenHash(sha256(rawToken));
        token.setPinHash(sha256(pin));
        token.setPinAttempts(0);
        token.setPurpose(EmailVerificationPurpose.EMAIL_CHANGE);
        token.setExpiresAt(LocalDateTime.now().plusHours(registrationTtlHours));
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        String confirmLink = buildEmailChangeLink(rawToken);
        emailSender.sendEmailChangeConfirmation(normalized, user.getName(), confirmLink, pin);
        emailSender.sendEmailChangeNotice(user.getEmail(), user.getName(), normalized);
    }

    // --- ConfirmEmailChangeUseCase ---
    @Override
    public void confirmByLink(UUID userId, String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException("Invalid or expired token");
        }
        String tokenHash = sha256(rawToken);
        EmailVerificationToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invalid or expired token"));
        if (!token.getUserId().equals(userId)) {
            throw new DomainException("Invalid or expired token");
        }
        if (token.isUsed() || token.isExpired()) {
            throw new DomainException("Invalid or expired token");
        }
        if (token.getPurpose() != EmailVerificationPurpose.EMAIL_CHANGE) {
            throw new DomainException("Invalid or expired token");
        }
        applyEmailChange(userId, token);
    }

    @Override
    public void confirmByPin(UUID userId, String pin) {
        if (pin == null || pin.isBlank()) {
            throw new DomainException("Invalid or expired PIN");
        }
        EmailVerificationToken token = tokenRepository.findActiveByUserAndPurpose(userId, EmailVerificationPurpose.EMAIL_CHANGE)
                .orElseThrow(() -> new DomainException("Invalid or expired PIN"));
        if (token.isExpired() || token.isUsed()) {
            throw new DomainException("Invalid or expired PIN");
        }
        token.setPinAttempts(token.getPinAttempts() + 1);
        boolean match = constantTimeEquals(sha256(pin), token.getPinHash());
        if (!match) {
            if (token.getPinAttempts() >= pinMaxAttempts) {
                token.setUsedAt(LocalDateTime.now());
            }
            tokenRepository.save(token);
            throw new DomainException("Invalid or expired PIN");
        }
        applyEmailChange(userId, token);
    }

    // --- internal helpers ---

    private void sendVerification(User user) {
        tokenRepository.invalidateActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.REGISTRATION);

        String rawToken = generateToken();
        String pin = generatePin();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setTargetEmail(user.getEmail());
        token.setTokenHash(sha256(rawToken));
        token.setPinHash(sha256(pin));
        token.setPinAttempts(0);
        token.setPurpose(EmailVerificationPurpose.REGISTRATION);
        token.setExpiresAt(LocalDateTime.now().plusHours(registrationTtlHours));
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        String verifyLink = buildVerifyLink(rawToken);
        emailSender.sendEmailVerificationEmail(user.getEmail(), user.getName(), verifyLink, pin);
    }

    private void verifyTokenAndMarkUser(EmailVerificationToken token) {
        if (token.isUsed() || token.isExpired()) {
            throw new DomainException("Invalid or expired verification token");
        }
        if (token.getPurpose() != EmailVerificationPurpose.REGISTRATION) {
            throw new DomainException("Invalid or expired verification token");
        }
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new DomainException("Invalid or expired verification token"));

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        markUserVerified(user);
    }

    private void markUserVerified(User user) {
        if (user.isEmailVerified()) {
            return;
        }
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String generatePin() {
        int n = secureRandom.nextInt(1_000_000);
        return String.format(Locale.ROOT, "%06d", n);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private String buildVerifyLink(String token) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return base + "/auth/verify-email?token=" + token;
    }

    private String buildEmailChangeLink(String token) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return base + "/auth/confirm-email-change?token=" + token;
    }

    private void applyEmailChange(UUID userId, EmailVerificationToken token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        user.setEmail(token.getTargetEmail());
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
