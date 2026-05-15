package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.model.PasswordResetToken;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.in.RequestPasswordResetUseCase;
import com.orizon.coreapi.domain.port.in.ResetPasswordUseCase;
import com.orizon.coreapi.domain.port.out.EmailSender;
import com.orizon.coreapi.domain.port.out.PasswordEncoder;
import com.orizon.coreapi.domain.port.out.PasswordResetTokenRepository;
import com.orizon.coreapi.domain.port.out.RefreshTokenRepository;
import com.orizon.coreapi.domain.port.out.UserRepository;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

public class PasswordResetService implements RequestPasswordResetUseCase, ResetPasswordUseCase {

    private static final Logger LOG = Logger.getLogger(PasswordResetService.class);
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailSender emailSender;
    private final int tokenTtlMinutes;
    private final String frontendBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                RefreshTokenRepository refreshTokenRepository,
                                EmailSender emailSender,
                                int tokenTtlMinutes,
                                String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailSender = emailSender;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void execute(String email) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            // Silent no-op to avoid email enumeration.
            LOG.debugf("Password reset requested for unknown email: %s", email);
            return;
        }

        User user = maybeUser.get();
        tokenRepository.invalidateAllByUserId(user.getId());

        String rawToken = generateToken();
        String tokenHash = sha256(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(tokenTtlMinutes));
        token.setUsedAt(null);
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        String resetLink = buildResetLink(rawToken);
        emailSender.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
    }

    @Override
    public void execute(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException("Invalid or expired reset token");
        }
        String tokenHash = sha256(rawToken);
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invalid or expired reset token"));

        if (token.isUsed() || token.isExpired()) {
            throw new DomainException("Invalid or expired reset token");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new DomainException("Invalid or expired reset token"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
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

    private String buildResetLink(String token) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return base + "/auth/new-password?token=" + token;
    }
}
