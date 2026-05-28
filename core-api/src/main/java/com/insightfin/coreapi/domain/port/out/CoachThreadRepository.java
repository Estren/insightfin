package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.CoachThread;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoachThreadRepository {
    CoachThread save(CoachThread thread);
    Optional<CoachThread> findById(UUID id);
    List<CoachThread> findByUserIdOrderByLastMessageDesc(UUID userId);
    void deleteById(UUID id);
}
