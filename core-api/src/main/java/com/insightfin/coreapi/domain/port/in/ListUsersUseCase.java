package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.User;

import java.util.List;

public interface ListUsersUseCase {
    List<User> execute();
}
