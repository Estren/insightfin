package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.AuthTokens;
import com.orizon.coreapi.domain.model.RefreshToken;
import com.orizon.coreapi.domain.model.Role;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.in.RequestEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.out.AvatarStoragePort;
import com.orizon.coreapi.domain.port.out.PasswordEncoder;
import com.orizon.coreapi.domain.port.out.RefreshTokenRepository;
import com.orizon.coreapi.domain.port.out.TokenProvider;
import com.orizon.coreapi.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenProvider tokenProvider;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock AvatarStoragePort avatarStoragePort;
    @Mock RequestEmailVerificationUseCase requestEmailVerificationUseCase;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder, tokenProvider,
                refreshTokenRepository, avatarStoragePort, requestEmailVerificationUseCase, true);
    }

    // --- U1 ---
    @Test
    void createUser_succeeds_encodesPasswordAndSaves() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        User saved = buildUser(UUID.randomUUID(), "John", "john@example.com", "hashed");
        when(userRepository.save(any())).thenReturn(saved);

        User result = service.execute("John", "john@example.com", "secret");

        assertThat(result).isEqualTo(saved);
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(any(User.class));
    }

    // --- U2 ---
    @Test
    void createUser_throwsWhenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute("John", "john@example.com", "secret"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // --- U3 ---
    @Test
    void authenticate_succeeds_returnsTokens() {
        User user = buildUser(UUID.randomUUID(), "John", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified()))
                .thenReturn("access-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthTokens tokens = service.execute("john@example.com", "secret");

        assertThat(tokens.getAccessToken()).isEqualTo("access-token");
        assertThat(tokens.getRefreshToken()).isNotBlank();
    }

    // --- U4 ---
    @Test
    void authenticate_throwsWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("unknown@example.com", "secret"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // --- U5 ---
    @Test
    void authenticate_throwsWhenPasswordMismatch() {
        User user = buildUser(UUID.randomUUID(), "John", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.execute("john@example.com", "wrong"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // --- U6 ---
    @Test
    void refreshToken_throwsWhenRevoked() {
        RefreshToken revoked = buildRefreshToken(UUID.randomUUID(), true, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("token-value")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.execute("token-value"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("revoked");
    }

    // --- U7 ---
    @Test
    void changePassword_throwsWhenCurrentPasswordWrong() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "John", "john@example.com", "hashed");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(userId, "wrong", "newpass"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    // --- U8 ---
    @Test
    void update_savesNewName() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "John", "john@example.com", "hashed");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.update(userId, "Johnny");

        assertThat(result.getName()).isEqualTo("Johnny");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    // --- U9: case-insensitive duplicate detection on register ---
    @Test
    void createUser_throwsWhenEmailAlreadyRegisteredWithDifferentCase() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute("John", "JOHN@Example.com", "secret"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // --- U10: register normalizes email before saving (lowercase + trim) ---
    @Test
    void createUser_normalizesEmailBeforeSaving() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.execute("John", "  JOHN@Example.com  ", "secret");

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    // --- U11: authenticate accepts the same email in different case ---
    @Test
    void authenticate_succeedsWithDifferentCase() {
        User user = buildUser(UUID.randomUUID(), "John", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified()))
                .thenReturn("access-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthTokens tokens = service.execute("JOHN@Example.com", "secret");

        assertThat(tokens.getAccessToken()).isEqualTo("access-token");
    }

    // --- U12: update throws when user not found ---
    @Test
    void update_throwsWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(userId, "Johnny"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    // --- fixtures ---

    private User buildUser(UUID id, String name, String email, String passwordHash) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(Role.USER);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return u;
    }

    private RefreshToken buildRefreshToken(UUID userId, boolean revoked, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID());
        rt.setUserId(userId);
        rt.setToken("token-value");
        rt.setExpiresAt(expiresAt);
        rt.setRevoked(revoked);
        rt.setCreatedAt(LocalDateTime.now());
        return rt;
    }
}
