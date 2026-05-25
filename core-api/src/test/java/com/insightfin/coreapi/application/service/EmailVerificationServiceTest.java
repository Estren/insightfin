package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.exception.DuplicateResourceException;
import com.insightfin.coreapi.domain.model.EmailVerificationPurpose;
import com.insightfin.coreapi.domain.model.EmailVerificationToken;
import com.insightfin.coreapi.domain.model.Role;
import com.insightfin.coreapi.domain.model.User;
import com.insightfin.coreapi.domain.port.out.EmailSender;
import com.insightfin.coreapi.domain.port.out.EmailVerificationTokenRepository;
import com.insightfin.coreapi.domain.port.out.RefreshTokenRepository;
import com.insightfin.coreapi.domain.port.out.UserRepository;
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
class EmailVerificationServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmailVerificationTokenRepository tokenRepository;
    @Mock EmailSender emailSender;
    @Mock RefreshTokenRepository refreshTokenRepository;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(userRepository, tokenRepository, emailSender,
                refreshTokenRepository, 24, 5, "http://localhost:4200");
    }

    @Test
    void requestVerification_unknownUser_throws() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(userId))
                .isInstanceOf(DomainException.class);

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void requestVerification_alreadyVerified_isNoOp() {
        User user = buildUser(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.execute(user.getId());

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void requestVerification_savesTokenWithPinAndSendsEmail() {
        User user = buildUser(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.execute(user.getId());

        verify(tokenRepository).invalidateActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.REGISTRATION);
        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getTokenHash()).isNotBlank();
        assertThat(saved.getPinHash()).isNotBlank();
        assertThat(saved.getPinAttempts()).isZero();
        assertThat(saved.getPurpose()).isEqualTo(EmailVerificationPurpose.REGISTRATION);
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pinCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendEmailVerificationEmail(eq(user.getEmail()), eq(user.getName()),
                linkCaptor.capture(), pinCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:4200/auth/verify-email?token=");
        assertThat(pinCaptor.getValue()).matches("\\d{6}");
    }

    @Test
    void resendVerification_unknownEmail_isSilentNoOp() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        service.execute("ghost@example.com");

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void resendVerification_alreadyVerified_isNoOp() {
        User user = buildUser(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        service.execute(user.getEmail());

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void resendVerification_normalizesEmailAndSends() {
        User user = buildUser(false);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        service.execute("  JOHN@Example.com  ");

        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailSender).sendEmailVerificationEmail(eq(user.getEmail()), eq(user.getName()),
                anyString(), anyString());
    }

    @Test
    void confirmByLink_validToken_marksUserVerified() {
        User user = buildUser(false);
        EmailVerificationToken token = buildToken(user.getId(), false, false, EmailVerificationPurpose.REGISTRATION);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.confirmByLink("some-token");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        verify(userRepository).save(user);
        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUsedAt()).isNotNull();
    }

    @Test
    void confirmByLink_expiredToken_throws() {
        EmailVerificationToken token = buildToken(UUID.randomUUID(), true, false, EmailVerificationPurpose.REGISTRATION);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByLink("some-token"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmByLink_usedToken_throws() {
        EmailVerificationToken token = buildToken(UUID.randomUUID(), false, true, EmailVerificationPurpose.REGISTRATION);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByLink("some-token"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmByLink_wrongPurpose_throws() {
        // A token created for EMAIL_CHANGE cannot be used to verify registration via this endpoint.
        EmailVerificationToken token = buildToken(UUID.randomUUID(), false, false, EmailVerificationPurpose.EMAIL_CHANGE);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByLink("some-token"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmByPin_validPin_marksUserVerified() {
        User user = buildUser(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Pre-build a token whose pinHash matches the SHA-256 of "123456" — we replicate the same hashing.
        String knownPin = "123456";
        String knownPinHash = sha256Hex(knownPin);
        EmailVerificationToken token = buildToken(user.getId(), false, false, EmailVerificationPurpose.REGISTRATION);
        token.setPinHash(knownPinHash);
        when(tokenRepository.findActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));

        service.confirmByPin(user.getEmail(), knownPin);

        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void confirmByPin_wrongPin_incrementsAttempts() {
        User user = buildUser(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        EmailVerificationToken token = buildToken(user.getId(), false, false, EmailVerificationPurpose.REGISTRATION);
        token.setPinHash(sha256Hex("123456"));
        token.setPinAttempts(1);
        when(tokenRepository.findActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByPin(user.getEmail(), "999999"))
                .isInstanceOf(DomainException.class);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getPinAttempts()).isEqualTo(2);
        assertThat(captor.getValue().getUsedAt()).isNull();   // not invalidated yet
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmByPin_maxAttemptsReached_invalidatesToken() {
        User user = buildUser(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        EmailVerificationToken token = buildToken(user.getId(), false, false, EmailVerificationPurpose.REGISTRATION);
        token.setPinHash(sha256Hex("123456"));
        token.setPinAttempts(4);   // already at 4; next wrong attempt hits 5 (max)
        when(tokenRepository.findActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByPin(user.getEmail(), "999999"))
                .isInstanceOf(DomainException.class);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getPinAttempts()).isEqualTo(5);
        assertThat(captor.getValue().getUsedAt()).isNotNull();
    }

    @Test
    void confirmByPin_unknownEmail_throws() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmByPin("ghost@example.com", "123456"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmByPin_noActiveToken_throws() {
        User user = buildUser(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmByPin(user.getEmail(), "123456"))
                .isInstanceOf(DomainException.class);
    }

    // ============================================================
    // Email change flow
    // ============================================================

    @Test
    void requestEmailChange_unknownUser_throws() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(userId, "new@example.com"))
                .isInstanceOf(DomainException.class);

        verifyNoInteractions(tokenRepository, emailSender, refreshTokenRepository);
    }

    @Test
    void requestEmailChange_sameEmail_throws() {
        User user = buildUser(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(user.getId(), "  JOHN@Example.com  "))
                .isInstanceOf(DomainException.class);

        verifyNoInteractions(tokenRepository, emailSender, refreshTokenRepository);
    }

    @Test
    void requestEmailChange_takenEmail_throws() {
        User user = buildUser(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(user.getId(), "new@example.com"))
                .isInstanceOf(DuplicateResourceException.class);

        verifyNoInteractions(emailSender, refreshTokenRepository);
    }

    @Test
    void requestEmailChange_success_savesTokenAndSendsBothEmails() {
        User user = buildUser(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        service.execute(user.getId(), "  NEW@Example.com  ");

        verify(tokenRepository).invalidateActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.EMAIL_CHANGE);
        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getTargetEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPurpose()).isEqualTo(EmailVerificationPurpose.EMAIL_CHANGE);
        assertThat(saved.getTokenHash()).isNotBlank();
        assertThat(saved.getPinHash()).isNotBlank();

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pinCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendEmailChangeConfirmation(eq("new@example.com"), eq(user.getName()),
                linkCaptor.capture(), pinCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:4200/auth/confirm-email-change?token=");
        assertThat(pinCaptor.getValue()).matches("\\d{6}");

        verify(emailSender).sendEmailChangeNotice(user.getEmail(), user.getName(), "new@example.com");
        // refresh tokens stay alive until the change is actually confirmed
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void confirmEmailChangeByLink_valid_updatesEmailAndRevokesRefreshTokens() {
        User user = buildUser(true);
        EmailVerificationToken token = buildToken(user.getId(), false, false, EmailVerificationPurpose.EMAIL_CHANGE);
        token.setTargetEmail("new@example.com");
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.confirmByLink(user.getId(), "raw-token");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    void confirmEmailChangeByLink_wrongUserId_throws() {
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();
        EmailVerificationToken token = buildToken(realOwner, false, false, EmailVerificationPurpose.EMAIL_CHANGE);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByLink(attacker, "raw-token"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void confirmEmailChangeByLink_wrongPurpose_throws() {
        // Registration token cannot be reused as an email-change confirmation.
        UUID userId = UUID.randomUUID();
        EmailVerificationToken token = buildToken(userId, false, false, EmailVerificationPurpose.REGISTRATION);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByLink(userId, "raw-token"))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void confirmEmailChangeByPin_valid_updatesEmailAndRevokesRefreshTokens() {
        User user = buildUser(true);
        EmailVerificationToken token = buildToken(user.getId(), false, false, EmailVerificationPurpose.EMAIL_CHANGE);
        token.setTargetEmail("new@example.com");
        token.setPinHash(sha256Hex("123456"));
        when(tokenRepository.findActiveByUserAndPurpose(user.getId(), EmailVerificationPurpose.EMAIL_CHANGE))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.confirmByPin(user.getId(), "123456");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    void confirmEmailChangeByPin_wrongPin_incrementsAttempts() {
        UUID userId = UUID.randomUUID();
        EmailVerificationToken token = buildToken(userId, false, false, EmailVerificationPurpose.EMAIL_CHANGE);
        token.setPinHash(sha256Hex("123456"));
        token.setPinAttempts(1);
        when(tokenRepository.findActiveByUserAndPurpose(userId, EmailVerificationPurpose.EMAIL_CHANGE))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByPin(userId, "999999"))
                .isInstanceOf(DomainException.class);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getPinAttempts()).isEqualTo(2);
        assertThat(captor.getValue().getUsedAt()).isNull();
        verify(userRepository, never()).save(any());
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void confirmEmailChangeByPin_maxAttempts_invalidatesToken() {
        UUID userId = UUID.randomUUID();
        EmailVerificationToken token = buildToken(userId, false, false, EmailVerificationPurpose.EMAIL_CHANGE);
        token.setPinHash(sha256Hex("123456"));
        token.setPinAttempts(4);
        when(tokenRepository.findActiveByUserAndPurpose(userId, EmailVerificationPurpose.EMAIL_CHANGE))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmByPin(userId, "999999"))
                .isInstanceOf(DomainException.class);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getPinAttempts()).isEqualTo(5);
        assertThat(captor.getValue().getUsedAt()).isNotNull();
        verify(userRepository, never()).save(any());
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void confirmEmailChangeByPin_noActiveToken_throws() {
        UUID userId = UUID.randomUUID();
        when(tokenRepository.findActiveByUserAndPurpose(userId, EmailVerificationPurpose.EMAIL_CHANGE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmByPin(userId, "123456"))
                .isInstanceOf(DomainException.class);

        verifyNoInteractions(refreshTokenRepository);
    }

    // --- helpers ---

    private User buildUser(boolean verified) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.USER);
        user.setEmailVerified(verified);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return user;
    }

    private EmailVerificationToken buildToken(UUID userId, boolean expired, boolean used,
                                              EmailVerificationPurpose purpose) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setTargetEmail("john@example.com");
        token.setTokenHash("token-hash");
        token.setPinHash("pin-hash");
        token.setPinAttempts(0);
        token.setPurpose(purpose);
        token.setExpiresAt(expired
                ? LocalDateTime.now().minusMinutes(5)
                : LocalDateTime.now().plusHours(1));
        token.setUsedAt(used ? LocalDateTime.now().minusMinutes(5) : null);
        token.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        return token;
    }

    private static String sha256Hex(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
