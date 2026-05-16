package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.AuthTokens;
import com.orizon.coreapi.domain.model.RefreshToken;
import com.orizon.coreapi.domain.model.Role;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.in.AuthenticateUserUseCase;
import com.orizon.coreapi.domain.port.in.ChangePasswordUseCase;
import com.orizon.coreapi.domain.port.in.CreateUserUseCase;
import com.orizon.coreapi.domain.port.in.DeleteUserUseCase;
import com.orizon.coreapi.domain.port.in.GetCurrentUserUseCase;
import com.orizon.coreapi.domain.port.in.ListUsersUseCase;
import com.orizon.coreapi.domain.port.in.LogoutUseCase;
import com.orizon.coreapi.domain.port.in.RefreshTokenUseCase;
import com.orizon.coreapi.domain.port.in.RequestEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.in.UpdateUserUseCase;
import com.orizon.coreapi.domain.port.in.UploadAvatarUseCase;
import com.orizon.coreapi.domain.port.out.AvatarStoragePort;
import com.orizon.coreapi.domain.port.out.PasswordEncoder;
import com.orizon.coreapi.domain.port.out.RefreshTokenRepository;
import com.orizon.coreapi.domain.port.out.TokenProvider;
import com.orizon.coreapi.domain.port.out.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UserService implements CreateUserUseCase, AuthenticateUserUseCase,
        RefreshTokenUseCase, LogoutUseCase,
        GetCurrentUserUseCase, UpdateUserUseCase, DeleteUserUseCase, ChangePasswordUseCase,
        ListUsersUseCase, UploadAvatarUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AvatarStoragePort avatarStoragePort;
    private final RequestEmailVerificationUseCase requestEmailVerificationUseCase;
    private final boolean emailVerificationRequired;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository,
                       AvatarStoragePort avatarStoragePort,
                       RequestEmailVerificationUseCase requestEmailVerificationUseCase,
                       boolean emailVerificationRequired) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.avatarStoragePort = avatarStoragePort;
        this.requestEmailVerificationUseCase = requestEmailVerificationUseCase;
        this.emailVerificationRequired = emailVerificationRequired;
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

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new DomainException("Invalid email or password");
        }

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified());
        String refreshToken = createRefreshToken(user.getId());

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public String execute(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new DomainException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new DomainException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DomainException("Refresh token has expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new DomainException("User not found"));

        return tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified());
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
    public User update(UUID userId, String name, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String normalizedEmail = normalizeEmail(email);
        if (!user.getEmail().equals(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already registered: " + normalizedEmail);
        }

        user.setName(name);
        user.setEmail(normalizedEmail);
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
}
