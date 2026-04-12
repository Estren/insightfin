package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.BudgetResponse;
import com.orizon.coreapi.adapter.in.web.dto.BudgetStatusResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateBudgetRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.port.in.CreateBudgetUseCase;
import com.orizon.coreapi.domain.port.in.GetBudgetStatusUseCase;
import com.orizon.coreapi.domain.port.in.ListBudgetsUseCase;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    AuthenticatedUser authenticatedUser;

    @Inject
    CategoryRepository categoryRepository;

    @POST
    public Response create(@Valid CreateBudgetRequest request) {
        var budget = createBudgetUseCase.execute(
                authenticatedUser.getUserId(), request.categoryId(), request.amount(), request.month());
        var categoryName = categoryRepository.findById(request.categoryId())
                .map(Category::getName).orElse("Unknown");
        return Response.status(Response.Status.CREATED)
                .entity(WebMapper.toResponse(budget, categoryName)).build();
    }

    @GET
    public List<BudgetResponse> list(@QueryParam("month") String month) {
        Map<UUID, String> categoryNames = categoryRepository.findByUserId(authenticatedUser.getUserId())
                .stream().collect(Collectors.toMap(Category::getId, Category::getName));
        return listBudgetsUseCase.execute(authenticatedUser.getUserId(), month)
                .stream()
                .map(b -> WebMapper.toResponse(b, categoryNames.getOrDefault(b.getCategoryId(), "Unknown")))
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
}
