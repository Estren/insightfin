package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.*;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.port.in.ContributeToGoalUseCase;
import com.orizon.coreapi.domain.port.in.CreateGoalUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final CreateGoalUseCase createGoalUseCase;
    private final ContributeToGoalUseCase contributeToGoalUseCase;

    public GoalController(CreateGoalUseCase createGoalUseCase,
                          ContributeToGoalUseCase contributeToGoalUseCase) {
        this.createGoalUseCase = createGoalUseCase;
        this.contributeToGoalUseCase = contributeToGoalUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@RequestAttribute UUID userId,
                               @Valid @RequestBody CreateGoalRequest request) {
        var goal = createGoalUseCase.execute(
                userId, request.title(), request.targetAmount(), request.deadline());
        return WebMapper.toResponse(goal);
    }

    @PostMapping("/{goalId}/contributions")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalContributionResponse contribute(@PathVariable UUID goalId,
                                               @Valid @RequestBody CreateGoalContributionRequest request) {
        var contribution = contributeToGoalUseCase.execute(goalId, request.amount(), request.date());
        return WebMapper.toResponse(contribution);
    }
}
