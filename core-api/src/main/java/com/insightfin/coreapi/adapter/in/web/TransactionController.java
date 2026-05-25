package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.CreateTransactionRequest;
import com.insightfin.coreapi.adapter.in.web.dto.TransactionResponse;
import com.insightfin.coreapi.adapter.in.web.dto.UpdateTransactionRequest;
import com.insightfin.coreapi.adapter.in.web.mapper.WebMapper;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.port.in.CreateTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.ListTransactionsUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateTransactionUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    ListTransactionsUseCase listTransactionsUseCase;

    @Inject
    UpdateTransactionUseCase updateTransactionUseCase;

    @Inject
    DeleteTransactionUseCase deleteTransactionUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @Inject
    CategoryRepository categoryRepository;

    private Map<UUID, String> loadCategoryNames(UUID userId) {
        return categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private String resolveCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown");
    }

    @POST
    public Response create(@Valid CreateTransactionRequest request) {
        var transaction = createTransactionUseCase.execute(
                authenticatedUser.getUserId(), request.categoryId(), request.type(),
                request.amount(), request.description(), request.date());
        return Response.status(Response.Status.CREATED)
                .entity(WebMapper.toResponse(transaction, resolveCategoryName(transaction.getCategoryId())))
                .build();
    }

    @GET
    public List<TransactionResponse> list(@QueryParam("startDate") LocalDate startDate,
                                          @QueryParam("endDate") LocalDate endDate) {
        var categoryNames = loadCategoryNames(authenticatedUser.getUserId());
        return listTransactionsUseCase.execute(authenticatedUser.getUserId(), startDate, endDate)
                .stream()
                .map(t -> WebMapper.toResponse(t, categoryNames.getOrDefault(t.getCategoryId(), "Unknown")))
                .toList();
    }

    @PUT
    @Path("/{id}")
    public TransactionResponse update(@PathParam("id") UUID id, @Valid UpdateTransactionRequest request) {
        var transaction = updateTransactionUseCase.execute(
                authenticatedUser.getUserId(), id, request.categoryId(), request.type(),
                request.amount(), request.description(), request.date());
        return WebMapper.toResponse(transaction, resolveCategoryName(transaction.getCategoryId()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteTransactionUseCase.execute(authenticatedUser.getUserId(), id);
        return Response.noContent().build();
    }
}
