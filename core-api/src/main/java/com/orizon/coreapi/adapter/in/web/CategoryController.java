package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.CategoryResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateCategoryRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.in.CreateCategoryUseCase;
import com.orizon.coreapi.domain.port.in.ListCategoriesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase,
                              ListCategoriesUseCase listCategoriesUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@RequestAttribute UUID userId,
                                   @Valid @RequestBody CreateCategoryRequest request) {
        var category = createCategoryUseCase.execute(
                userId, request.name(), request.type(), request.icon(), request.color());
        return WebMapper.toResponse(category);
    }

    @GetMapping
    public List<CategoryResponse> list(@RequestAttribute UUID userId,
                                       @RequestParam(required = false) TransactionType type) {
        return listCategoriesUseCase.execute(userId, type)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
