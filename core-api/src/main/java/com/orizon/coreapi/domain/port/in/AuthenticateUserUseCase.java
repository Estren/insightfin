package com.orizon.coreapi.domain.port.in;

public interface AuthenticateUserUseCase {
    String execute(String email, String password);
}
