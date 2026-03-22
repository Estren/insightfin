package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.in.AuthenticateUserUseCase;
import com.orizon.coreapi.domain.port.in.CreateUserUseCase;
import com.orizon.coreapi.domain.port.out.PasswordEncoder;
import com.orizon.coreapi.domain.port.out.TokenProvider;
import com.orizon.coreapi.domain.port.out.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserService implements CreateUserUseCase, AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public User execute(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public String execute(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new DomainException("Invalid email or password");
        }

        return tokenProvider.generateToken(user.getId(), user.getEmail());
    }
}
