package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.model.PasswordResetToken;
import com.orizon.coreapi.domain.model.Role;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.out.EmailSender;
import com.orizon.coreapi.domain.port.out.PasswordEncoder;
import com.orizon.coreapi.domain.port.out.PasswordResetTokenRepository;
import com.orizon.coreapi.domain.port.out.RefreshTokenRepository;
import com.orizon.coreapi.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock EmailSender emailSender;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(userRepository, tokenRepository, passwordEncoder,
                refreshTokenRepository, emailSender, 30, "http://localhost:4200");
    }

    @Test
    void requestReset_unknownEmail_isNoOp() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        service.execute("ghost@example.com");

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void requestReset_knownEmail_persistsTokenAndSendsEmail() {
        User user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        service.execute(user.getEmail());

        verify(tokenRepository).invalidateAllByUserId(user.getId());
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getTokenHash()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(saved.getUsedAt()).isNull();

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendPasswordResetEmail(eq(user.getEmail()), eq(user.getName()), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:4200/auth/new-password?token=");
    }

    @Test
    void resetPassword_invalidToken_throws() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bogus", "newpass1234"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_expiredToken_throws() {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(UUID.randomUUID());
        token.setTokenHash("hash");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        token.setCreatedAt(LocalDateTime.now().minusMinutes(31));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute("some-token", "newpass1234"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void resetPassword_usedToken_throws() {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(UUID.randomUUID());
        token.setTokenHash("hash");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsedAt(LocalDateTime.now().minusMinutes(5));
        token.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute("some-token", "newpass1234"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndRevokesRefreshTokens() {
        User user = buildUser();
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setTokenHash("hash");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setCreatedAt(LocalDateTime.now());
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass1234")).thenReturn("encoded");

        service.execute("some-token", "newpass1234");

        assertThat(user.getPasswordHash()).isEqualTo("encoded");
        verify(userRepository).save(user);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUsedAt()).isNotNull();
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return user;
    }
}
