package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.AuthRequest;
import com.orizon.coreapi.adapter.in.web.dto.AuthResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateUserRequest;
import com.orizon.coreapi.adapter.in.web.dto.UserResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.port.in.AuthenticateUserUseCase;
import com.orizon.coreapi.domain.port.in.CreateUserUseCase;
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

    @POST
    @Path("/register")
    public Response register(@Valid CreateUserRequest request) {
        var user = createUserUseCase.execute(request.name(), request.email(), request.password());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(user)).build();
    }

    @POST
    @Path("/login")
    public AuthResponse login(@Valid AuthRequest request) {
        var token = authenticateUserUseCase.execute(request.email(), request.password());
        return new AuthResponse(token);
    }
}
