package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.AuthRequest;
import com.orizon.coreapi.adapter.in.web.dto.AuthResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateUserRequest;
import com.orizon.coreapi.adapter.in.web.dto.UserResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.port.in.AuthenticateUserUseCase;
import com.orizon.coreapi.domain.port.in.CreateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CreateUserUseCase createUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(CreateUserUseCase createUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        var user = createUserUseCase.execute(request.name(), request.email(), request.password());
        return WebMapper.toResponse(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        var token = authenticateUserUseCase.execute(request.email(), request.password());
        return new AuthResponse(token);
    }
}
