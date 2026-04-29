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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    AuthenticatedUser authenticatedUser;

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
}
