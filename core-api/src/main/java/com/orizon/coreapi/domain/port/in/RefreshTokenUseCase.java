package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.AuthTokens;

public interface RefreshTokenUseCase {
    AuthTokens execute(String refreshToken);
}
