package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.BudgetResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateBudgetRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.port.in.CreateBudgetUseCase;
import com.orizon.coreapi.domain.port.in.ListBudgetsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final CreateBudgetUseCase createBudgetUseCase;
    private final ListBudgetsUseCase listBudgetsUseCase;

    public BudgetController(CreateBudgetUseCase createBudgetUseCase,
                            ListBudgetsUseCase listBudgetsUseCase) {
        this.createBudgetUseCase = createBudgetUseCase;
        this.listBudgetsUseCase = listBudgetsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@RequestAttribute UUID userId,
                                 @Valid @RequestBody CreateBudgetRequest request) {
        var budget = createBudgetUseCase.execute(
                userId, request.categoryId(), request.amount(), request.month());
        return WebMapper.toResponse(budget);
    }

    @GetMapping
    public List<BudgetResponse> list(@RequestAttribute UUID userId,
                                     @RequestParam String month) {
        return listBudgetsUseCase.execute(userId, month)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
