package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.AuthTokens;

public interface RefreshTokenUseCase {
    AuthTokens execute(String refreshToken);
}
