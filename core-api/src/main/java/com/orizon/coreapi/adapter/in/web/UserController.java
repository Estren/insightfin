package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.ChangePasswordRequest;
import com.orizon.coreapi.adapter.in.web.dto.UpdateUserRequest;
import com.orizon.coreapi.adapter.in.web.dto.UserResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.ChangePasswordUseCase;
import com.orizon.coreapi.domain.port.in.DeleteUserUseCase;
import com.orizon.coreapi.domain.port.in.GetCurrentUserUseCase;
import com.orizon.coreapi.domain.port.in.UpdateUserUseCase;
import com.orizon.coreapi.domain.port.in.UploadAvatarUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;

    @Inject
    UpdateUserUseCase updateUserUseCase;

    @Inject
    DeleteUserUseCase deleteUserUseCase;

    @Inject
    ChangePasswordUseCase changePasswordUseCase;

    @Inject
    UploadAvatarUseCase uploadAvatarUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    @GET
    @Path("/me")
    public UserResponse getMe() {
        var user = getCurrentUserUseCase.getCurrent(authenticatedUser.getUserId());
        return WebMapper.toResponse(user);
    }

    @PUT
    @Path("/me")
    public UserResponse updateMe(@Valid UpdateUserRequest request) {
        var user = updateUserUseCase.update(
                authenticatedUser.getUserId(), request.name(), request.email());
        return WebMapper.toResponse(user);
    }

    @DELETE
    @Path("/me")
    public Response deleteMe() {
        deleteUserUseCase.delete(authenticatedUser.getUserId());
        return Response.noContent().build();
    }

    @PUT
    @Path("/me/password")
    public Response changePassword(@Valid ChangePasswordRequest request) {
        changePasswordUseCase.changePassword(
                authenticatedUser.getUserId(),
                request.currentPassword(),
                request.newPassword());
        return Response.noContent().build();
    }

    @PUT
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public UserResponse uploadAvatar(@RestForm("file") FileUpload file) {
        if (file == null) {
            throw new BadRequestException("No file provided");
        }
        if (!ALLOWED_TYPES.contains(file.contentType())) {
            throw new BadRequestException("File must be jpeg, png, webp or gif");
        }
        try {
            byte[] data = Files.readAllBytes(file.filePath());
            if (data.length > MAX_SIZE_BYTES) {
                throw new BadRequestException("File must not exceed 5 MB");
            }
            var user = uploadAvatarUseCase.uploadAvatar(
                    authenticatedUser.getUserId(), file.fileName(), data, file.contentType());
            return WebMapper.toResponse(user);
        } catch (IOException e) {
            throw new jakarta.ws.rs.InternalServerErrorException("Failed to read uploaded file");
        }
    }
}
