package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.exception.DuplicateResourceException;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.AuthTokens;
import com.insightfin.coreapi.domain.model.GoogleAuthResult;
import com.insightfin.coreapi.domain.model.RefreshToken;
import com.insightfin.coreapi.domain.model.Role;
import com.insightfin.coreapi.domain.model.User;
import com.insightfin.coreapi.domain.port.in.AuthenticateUserUseCase;
import com.insightfin.coreapi.domain.port.in.AuthenticateWithGoogleUseCase;
import com.insightfin.coreapi.domain.port.in.ChangePasswordUseCase;
import com.insightfin.coreapi.domain.port.in.CreateUserUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteUserUseCase;
import com.insightfin.coreapi.domain.port.in.GetCurrentUserUseCase;
import com.insightfin.coreapi.domain.port.in.ListUsersUseCase;
import com.insightfin.coreapi.domain.port.in.LogoutUseCase;
import com.insightfin.coreapi.domain.port.in.RefreshTokenUseCase;
import com.insightfin.coreapi.domain.port.in.RequestEmailVerificationUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateUserUseCase;
import com.insightfin.coreapi.domain.port.in.UploadAvatarUseCase;
import com.insightfin.coreapi.domain.port.out.AvatarStoragePort;
import com.insightfin.coreapi.domain.port.out.GoogleTokenVerifier;
import com.insightfin.coreapi.domain.port.out.PasswordEncoder;
import com.insightfin.coreapi.domain.port.out.RefreshTokenRepository;
import com.insightfin.coreapi.domain.port.out.TokenProvider;
import com.insightfin.coreapi.domain.port.out.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UserService implements CreateUserUseCase, AuthenticateUserUseCase,
        AuthenticateWithGoogleUseCase,
        RefreshTokenUseCase, LogoutUseCase,
        GetCurrentUserUseCase, UpdateUserUseCase, DeleteUserUseCase, ChangePasswordUseCase,
        ListUsersUseCase, UploadAvatarUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AvatarStoragePort avatarStoragePort;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RequestEmailVerificationUseCase requestEmailVerificationUseCase;
    private final boolean emailVerificationRequired;
    private final int lockoutMaxAttempts;
    private final int lockoutDurationMinutes;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository,
                       AvatarStoragePort avatarStoragePort,
                       GoogleTokenVerifier googleTokenVerifier,
                       RequestEmailVerificationUseCase requestEmailVerificationUseCase,
                       boolean emailVerificationRequired,
                       int lockoutMaxAttempts,
                       int lockoutDurationMinutes) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.avatarStoragePort = avatarStoragePort;
        this.googleTokenVerifier = googleTokenVerifier;
        this.requestEmailVerificationUseCase = requestEmailVerificationUseCase;
        this.emailVerificationRequired = emailVerificationRequired;
        this.lockoutMaxAttempts = lockoutMaxAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    @Override
    public User execute(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already registered: " + normalizedEmail);
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setEmailVerified(!emailVerificationRequired);
        if (!emailVerificationRequired) {
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        if (emailVerificationRequired) {
            requestEmailVerificationUseCase.execute(saved.getId());
        }
        return saved;
    }

    @Override
    public AuthTokens execute(String email, String password) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new DomainException("Invalid email or password"));

        if (isLocked(user)) {
            throw new DomainException("Account temporarily locked due to repeated failed logins. Try again later.");
        }

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new DomainException("Invalid email or password");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified());
        String refreshToken = createRefreshToken(user.getId());

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public GoogleAuthResult authenticateWithGoogle(String idToken, String expectedNonce) {
        var info = googleTokenVerifier.verify(idToken, expectedNonce);
        String normalizedEmail = normalizeEmail(info.email());
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findByGoogleSub(info.sub()).orElse(null);
        boolean isNewUser = false;

        if (user == null) {
            user = userRepository.findByEmail(normalizedEmail).orElse(null);
            if (user == null) {
                isNewUser = true;
                user = new User();
                user.setId(UUID.randomUUID());
                user.setName(info.name() != null ? info.name() : normalizedEmail);
                user.setEmail(normalizedEmail);
                user.setPasswordHash(null);
                user.setRole(Role.USER);
                user.setEmailVerified(true);
                user.setEmailVerifiedAt(now);
                user.setGoogleSub(info.sub());
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
            } else {
                user.setGoogleSub(info.sub());
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                    user.setEmailVerifiedAt(now);
                }
                user.setUpdatedAt(now);
            }
        } else {
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                user.setEmailVerifiedAt(now);
            }
            user.setUpdatedAt(now);
        }

        User saved = userRepository.save(user);

        String accessToken = tokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole(), saved.isEmailVerified());
        String refreshToken = createRefreshToken(saved.getId());

        return new GoogleAuthResult(new AuthTokens(accessToken, refreshToken), isNewUser);
    }

    @Override
    public AuthTokens execute(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new DomainException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            // A revoked token presented again signals theft — revoke the whole
            // family so a stolen token cannot be used either.
            refreshTokenRepository.revokeAllByUserId(refreshToken.getUserId());
            throw new DomainException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DomainException("Refresh token has expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new DomainException("User not found"));

        // Rotate: revoke the used token and issue a fresh pair.
        refreshTokenRepository.revokeByToken(refreshTokenValue);

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified());
        String newRefreshToken = createRefreshToken(user.getId());

        return new AuthTokens(accessToken, newRefreshToken);
    }

    @Override
    public void execute(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    public User getCurrent(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Override
    public User update(UUID userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setName(name);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User", userId);
        }
        refreshTokenRepository.revokeAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getPasswordHash() == null) {
            throw new DomainException("No password set. Use forgot-password to define one.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new DomainException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    public List<User> execute() {
        return userRepository.findAll();
    }

    @Override
    public User uploadAvatar(UUID userId, String fileName, byte[] data, String contentType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getAvatarUrl() != null) {
            avatarStoragePort.delete(user.getAvatarUrl());
        }

        String avatarUrl = avatarStoragePort.upload(userId.toString(), fileName, data, contentType);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String createRefreshToken(UUID userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(LocalDateTime.now());

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private boolean isLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private void registerFailedAttempt(User user) {
        if (lockoutMaxAttempts <= 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int attempts = user.getFailedLoginAttempts();
        // A lock that already expired — start a fresh count.
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(now)) {
            attempts = 0;
            user.setLockedUntil(null);
        }
        attempts++;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= lockoutMaxAttempts) {
            user.setLockedUntil(now.plusMinutes(lockoutDurationMinutes));
        }
        user.setUpdatedAt(now);
        userRepository.save(user);
    }
}
