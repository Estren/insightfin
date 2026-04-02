package com.orizon.coreapi.domain.port.in;

public interface RefreshTokenUseCase {
    String execute(String refreshToken);
}
