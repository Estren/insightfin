package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.User;

import java.util.List;

public interface ListUsersUseCase {
    List<User> execute();
}
