package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.AuthTokens;

public interface AuthenticateUserUseCase {
    AuthTokens execute(String email, String password);
}
