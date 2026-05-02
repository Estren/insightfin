package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.AiFeedbackResponse;
import com.orizon.coreapi.adapter.in.web.dto.BudgetStatusResponse;
import com.orizon.coreapi.adapter.in.web.dto.CategoryResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateAiFeedbackRequest;
import com.orizon.coreapi.adapter.in.web.dto.GoalResponse;
import com.orizon.coreapi.adapter.in.web.dto.TransactionResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.port.in.CreateAiFeedbackUseCase;
import com.orizon.coreapi.domain.port.in.GetBudgetStatusUseCase;
import com.orizon.coreapi.domain.port.in.ListCategoriesUseCase;
import com.orizon.coreapi.domain.port.in.ListGoalsUseCase;
import com.orizon.coreapi.domain.port.in.ListTransactionsUseCase;
import com.orizon.coreapi.domain.port.in.ListUsersUseCase;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/internal")
@Produces(MediaType.APPLICATION_JSON)
public class InternalController {

    @Inject
    CreateAiFeedbackUseCase createAiFeedbackUseCase;

    @Inject
    ListUsersUseCase listUsersUseCase;

    @Inject
    ListTransactionsUseCase listTransactionsUseCase;

    @Inject
    GetBudgetStatusUseCase getBudgetStatusUseCase;

    @Inject
    ListGoalsUseCase listGoalsUseCase;

    @Inject
    ListCategoriesUseCase listCategoriesUseCase;

    @Inject
    CategoryRepository categoryRepository;

    @POST
    @Path("/feedbacks")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFeedback(@Valid CreateAiFeedbackRequest request) {
        var feedback = createAiFeedbackUseCase.execute(
                request.userId(), request.type(), request.title(),
                request.content(), request.metadata(), request.referenceMonth());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(feedback)).build();
    }

    @GET
    @Path("/users")
    public List<UUID> listUsers() {
        return listUsersUseCase.execute().stream()
                .map(user -> user.getId())
                .toList();
    }

    @GET
    @Path("/users/{id}/transactions")
    public List<TransactionResponse> listTransactions(@PathParam("id") UUID userId,
                                                      @QueryParam("month") String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Map<UUID, String> categoryNames = categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return listTransactionsUseCase.execute(userId, startDate, endDate).stream()
                .map(t -> WebMapper.toResponse(t, categoryNames.getOrDefault(t.getCategoryId(), "Unknown")))
                .toList();
    }

    @GET
    @Path("/users/{id}/budgets")
    public List<BudgetStatusResponse> listBudgetStatus(@PathParam("id") UUID userId,
                                                       @QueryParam("month") String month) {
        return getBudgetStatusUseCase.execute(userId, month, true).stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/users/{id}/goals")
    public List<GoalResponse> listGoals(@PathParam("id") UUID userId) {
        return listGoalsUseCase.execute(userId).stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/users/{id}/categories")
    public List<CategoryResponse> listCategories(@PathParam("id") UUID userId) {
        return listCategoriesUseCase.execute(userId, null).stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
