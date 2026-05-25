package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.CreateRecurringTransactionRequest;
import com.insightfin.coreapi.adapter.in.web.dto.RecurringTransactionResponse;
import com.insightfin.coreapi.adapter.in.web.dto.UpdateRecurringTransactionRequest;
import com.insightfin.coreapi.adapter.in.web.mapper.WebMapper;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.port.in.CreateRecurringTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteRecurringTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.GetRecurringTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.ListRecurringTransactionsUseCase;
import com.insightfin.coreapi.domain.port.in.PauseRecurringTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateRecurringTransactionUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/recurring-transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecurringTransactionController {

    @Inject
    CreateRecurringTransactionUseCase createUseCase;

    @Inject
    UpdateRecurringTransactionUseCase updateUseCase;

    @Inject
    DeleteRecurringTransactionUseCase deleteUseCase;

    @Inject
    ListRecurringTransactionsUseCase listUseCase;

    @Inject
    GetRecurringTransactionUseCase getUseCase;

    @Inject
    PauseRecurringTransactionUseCase pauseUseCase;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateRecurringTransactionRequest request) {
        var recurring = createUseCase.create(
                authenticatedUser.getUserId(), request.categoryId(), request.type(),
                request.amount(), request.description(), request.frequency(),
                request.startDate(), request.endDate());
        return Response.status(Response.Status.CREATED)
                .entity(WebMapper.toResponse(recurring, resolveCategoryName(recurring.getCategoryId())))
                .build();
    }

    @GET
    public List<RecurringTransactionResponse> list() {
        UUID userId = authenticatedUser.getUserId();
        Map<UUID, String> names = loadCategoryNames(userId);
        return listUseCase.list(userId).stream()
                .map(r -> WebMapper.toResponse(r, names.getOrDefault(r.getCategoryId(), "Unknown")))
                .toList();
    }

    @GET
    @Path("/{id}")
    public RecurringTransactionResponse get(@PathParam("id") UUID id) {
        var recurring = getUseCase.getById(authenticatedUser.getUserId(), id);
        return WebMapper.toResponse(recurring, resolveCategoryName(recurring.getCategoryId()));
    }

    @PUT
    @Path("/{id}")
    public RecurringTransactionResponse update(@PathParam("id") UUID id,
                                               @Valid UpdateRecurringTransactionRequest request) {
        var recurring = updateUseCase.update(
                authenticatedUser.getUserId(), id, request.categoryId(), request.type(),
                request.amount(), request.description(), request.frequency(),
                request.startDate(), request.endDate());
        return WebMapper.toResponse(recurring, resolveCategoryName(recurring.getCategoryId()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteUseCase.delete(authenticatedUser.getUserId(), id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/pause")
    public RecurringTransactionResponse pause(@PathParam("id") UUID id) {
        var recurring = pauseUseCase.pause(authenticatedUser.getUserId(), id);
        return WebMapper.toResponse(recurring, resolveCategoryName(recurring.getCategoryId()));
    }

    @POST
    @Path("/{id}/resume")
    public RecurringTransactionResponse resume(@PathParam("id") UUID id) {
        var recurring = pauseUseCase.resume(authenticatedUser.getUserId(), id);
        return WebMapper.toResponse(recurring, resolveCategoryName(recurring.getCategoryId()));
    }

    private Map<UUID, String> loadCategoryNames(UUID userId) {
        return categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private String resolveCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown");
    }
}
