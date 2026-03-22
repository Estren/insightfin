package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.CreateTransactionRequest;
import com.orizon.coreapi.adapter.in.web.dto.TransactionResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.port.in.CreateTransactionUseCase;
import com.orizon.coreapi.domain.port.in.ListTransactionsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 ListTransactionsUseCase listTransactionsUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@RequestAttribute UUID userId,
                                      @Valid @RequestBody CreateTransactionRequest request) {
        var transaction = createTransactionUseCase.execute(
                userId, request.categoryId(), request.type(),
                request.amount(), request.description(), request.date());
        return WebMapper.toResponse(transaction);
    }

    @GetMapping
    public List<TransactionResponse> list(@RequestAttribute UUID userId,
                                          @RequestParam LocalDate startDate,
                                          @RequestParam LocalDate endDate) {
        return listTransactionsUseCase.execute(userId, startDate, endDate)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
