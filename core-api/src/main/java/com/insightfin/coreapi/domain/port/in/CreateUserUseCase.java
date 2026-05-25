package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.User;

public interface CreateUserUseCase {
    User execute(String name, String email, String password);
}
