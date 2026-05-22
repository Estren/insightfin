package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.AuthRequest;
import com.orizon.coreapi.adapter.in.web.dto.AuthResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateUserRequest;
import com.orizon.coreapi.adapter.in.web.dto.ForgotPasswordRequest;
import com.orizon.coreapi.adapter.in.web.dto.GoogleSignInRequest;
import com.orizon.coreapi.adapter.in.web.dto.RefreshTokenRequest;
import com.orizon.coreapi.adapter.in.web.dto.ResendVerificationRequest;
import com.orizon.coreapi.adapter.in.web.dto.ResetPasswordRequest;
import com.orizon.coreapi.adapter.in.web.dto.VerifyEmailPinRequest;
import com.orizon.coreapi.adapter.in.web.dto.VerifyEmailRequest;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.AuthenticateUserUseCase;
import com.orizon.coreapi.domain.port.in.AuthenticateWithGoogleUseCase;
import com.orizon.coreapi.domain.port.in.ConfirmEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.in.CreateUserUseCase;
import com.orizon.coreapi.domain.port.in.LogoutUseCase;
import com.orizon.coreapi.domain.port.in.RefreshTokenUseCase;
import com.orizon.coreapi.domain.port.in.RequestPasswordResetUseCase;
import com.orizon.coreapi.domain.port.in.ResendEmailVerificationUseCase;
import com.orizon.coreapi.domain.port.in.ResetPasswordUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    CreateUserUseCase createUserUseCase;

    @Inject
    AuthenticateUserUseCase authenticateUserUseCase;

    @Inject
    AuthenticateWithGoogleUseCase authenticateWithGoogleUseCase;

    @Inject
    RefreshTokenUseCase refreshTokenUseCase;

    @Inject
    LogoutUseCase logoutUseCase;

    @Inject
    RequestPasswordResetUseCase requestPasswordResetUseCase;

    @Inject
    ResetPasswordUseCase resetPasswordUseCase;

    @Inject
    ConfirmEmailVerificationUseCase confirmEmailVerificationUseCase;

    @Inject
    ResendEmailVerificationUseCase resendEmailVerificationUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    @Path("/register")
    public Response register(@Valid CreateUserRequest request) {
        createUserUseCase.execute(request.name(), request.email(), request.password());
        var tokens = authenticateUserUseCase.execute(request.email(), request.password());
        return Response.status(Response.Status.CREATED)
                .entity(new AuthResponse(tokens.getAccessToken(), tokens.getRefreshToken()))
                .build();
    }

    @POST
    @Path("/login")
    public AuthResponse login(@Valid AuthRequest request) {
        var tokens = authenticateUserUseCase.execute(request.email(), request.password());
        return new AuthResponse(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    @POST
    @Path("/google")
    public AuthResponse google(@Valid GoogleSignInRequest request) {
        var result = authenticateWithGoogleUseCase.authenticateWithGoogle(request.credential(), request.nonce());
        var tokens = result.tokens();
        return new AuthResponse(tokens.getAccessToken(), tokens.getRefreshToken(), result.isNewUser());
    }

    @POST
    @Path("/refresh")
    public AuthResponse refresh(@Valid RefreshTokenRequest request) {
        var tokens = refreshTokenUseCase.execute(request.refreshToken());
        return new AuthResponse(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    @POST
    @Path("/logout")
    public Response logout() {
        logoutUseCase.execute(authenticatedUser.getUserId());
        return Response.noContent().build();
    }

    @POST
    @Path("/forgot-password")
    public Response forgotPassword(@Valid ForgotPasswordRequest request) {
        requestPasswordResetUseCase.execute(request.email());
        return Response.noContent().build();
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@Valid ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request.token(), request.password());
        return Response.noContent().build();
    }

    @POST
    @Path("/verify-email")
    public Response verifyEmail(@Valid VerifyEmailRequest request) {
        confirmEmailVerificationUseCase.confirmByLink(request.token());
        return Response.noContent().build();
    }

    @POST
    @Path("/verify-email-pin")
    public Response verifyEmailPin(@Valid VerifyEmailPinRequest request) {
        confirmEmailVerificationUseCase.confirmByPin(request.email(), request.pin());
        return Response.noContent().build();
    }

    @POST
    @Path("/resend-verification")
    public Response resendVerification(@Valid ResendVerificationRequest request) {
        resendEmailVerificationUseCase.execute(request.email());
        return Response.noContent().build();
    }
}
