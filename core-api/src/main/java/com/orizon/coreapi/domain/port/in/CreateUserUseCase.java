package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.User;

public interface CreateUserUseCase {
    User execute(String name, String email, String password);
}
