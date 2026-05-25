package com.insightfin.coreapi.adapter.in.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.insightfin.coreapi.adapter.in.web.dto.BudgetResponse;
import com.insightfin.coreapi.adapter.in.web.dto.BudgetStatusResponse;
import com.insightfin.coreapi.adapter.in.web.dto.CreateBudgetRequest;
import com.insightfin.coreapi.adapter.in.web.dto.UpdateBudgetRequest;
import com.insightfin.coreapi.adapter.in.web.mapper.WebMapper;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.port.in.CreateBudgetUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteBudgetUseCase;
import com.insightfin.coreapi.domain.port.in.GetBudgetStatusUseCase;
import com.insightfin.coreapi.domain.port.in.ListBudgetsUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateBudgetUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/budgets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BudgetController {

    @Inject
    CreateBudgetUseCase createBudgetUseCase;

    @Inject
    ListBudgetsUseCase listBudgetsUseCase;

    @Inject
    GetBudgetStatusUseCase getBudgetStatusUseCase;

    @Inject
    UpdateBudgetUseCase updateBudgetUseCase;

    @Inject
    DeleteBudgetUseCase deleteBudgetUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @Inject
    CategoryRepository categoryRepository;

    private String resolveCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getName).orElse("Unknown");
    }

    @POST
    public Response create(@Valid CreateBudgetRequest request) {
        var budget = createBudgetUseCase.execute(
                authenticatedUser.getUserId(), request.categoryId(), request.amount(), request.month());
        return Response.status(Response.Status.CREATED)
                .entity(WebMapper.toResponse(budget, resolveCategoryName(request.categoryId()))).build();
    }

    @GET
    public List<BudgetResponse> list(@QueryParam("month") String month) {
        Map<UUID, String> categoryNames = categoryRepository.findByUserId(authenticatedUser.getUserId())
                .stream().collect(Collectors.toMap(Category::getId, Category::getName));
        return listBudgetsUseCase.execute(authenticatedUser.getUserId(), month)
                .stream()
                .map(b -> WebMapper.toResponse(b,
                        categoryNames.getOrDefault(b.getCategoryId(), "Unknown")))
                .toList();
    }

    @GET
    @Path("/status")
    public List<BudgetStatusResponse> status(@QueryParam("month") String month) {
        return getBudgetStatusUseCase.execute(authenticatedUser.getUserId(), month, true)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @PUT
    @Path("/{id}")
    public BudgetResponse update(@PathParam("id") UUID id, @Valid UpdateBudgetRequest request) {
        var budget = updateBudgetUseCase.execute(
                authenticatedUser.getUserId(), id, request.amount());
        return WebMapper.toResponse(budget, resolveCategoryName(budget.getCategoryId()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteBudgetUseCase.execute(authenticatedUser.getUserId(), id);
        return Response.noContent().build();
    }
}
