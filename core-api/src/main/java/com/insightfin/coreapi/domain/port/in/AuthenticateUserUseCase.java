package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.AuthTokens;

public interface AuthenticateUserUseCase {
    AuthTokens execute(String email, String password);
}
