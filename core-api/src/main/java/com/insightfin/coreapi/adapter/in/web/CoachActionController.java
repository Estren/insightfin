package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.CoachActionRequest;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.model.Budget;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.Goal;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.in.ContributeToGoalUseCase;
import com.insightfin.coreapi.domain.port.in.CreateBudgetUseCase;
import com.insightfin.coreapi.domain.port.in.CreateGoalUseCase;
import com.insightfin.coreapi.domain.port.in.CreateTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.ListBudgetsUseCase;
import com.insightfin.coreapi.domain.port.in.ListGoalsUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateBudgetUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

/**
 * Executes a Coach-proposed write action after the user confirmed it in the UI.
 *
 * <p>This is the <strong>only</strong> mutation path for Coach actions, and it
 * is deliberately deterministic and LLM-free. The agent merely PROPOSES (see the
 * AI service's {@code propose_*} tools); execution happens here, gated by an
 * authenticated request that originates from the user's explicit confirmation
 * click. The acting user is always {@link AuthenticatedUser#getUserId()} from the
 * JWT — never anything the LLM produced — so a prompt-injected proposal cannot
 * touch another account and can at most do what the user could already do
 * through the normal UI. Validation reuses the existing domain use-cases.</p>
 */
@Path("/api/coach/actions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoachActionController {

    @Inject
    AuthenticatedUser authenticatedUser;

    @Inject
    CreateBudgetUseCase createBudgetUseCase;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    ContributeToGoalUseCase contributeToGoalUseCase;

    @Inject
    ListGoalsUseCase listGoalsUseCase;

    @Inject
    CreateGoalUseCase createGoalUseCase;

    @Inject
    UpdateBudgetUseCase updateBudgetUseCase;

    @Inject
    ListBudgetsUseCase listBudgetsUseCase;

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    private static final Logger LOG = Logger.getLogger(CoachActionController.class);

    @POST
    @Path("/execute")
    public Response execute(@Valid CoachActionRequest request) {
        UUID userId = authenticatedUser.getUserId();
        String action = request.action();
        Map<String, Object> params = request.params() != null ? request.params() : Map.of();

        LOG.infof("coach_action_execute userId=%s action=%s", userId, action);

        if ("create_budget".equals(action)) {
            return executeCreateBudget(userId, params);
        }
        if ("contribute_goal".equals(action)) {
            return executeContributeGoal(userId, params);
        }
        if ("create_goal".equals(action)) {
            return executeCreateGoal(userId, params);
        }
        if ("adjust_budget".equals(action)) {
            return executeAdjustBudget(userId, params);
        }
        if ("log_transaction".equals(action)) {
            return executeLogTransaction(userId, params);
        }
        throw new DomainException("Unsupported action: " + action);
    }

    private Response executeCreateBudget(UUID userId, Map<String, Object> params) {
        String categoryName = requireString(params.get("category"), "category");
        BigDecimal amount = requirePositiveAmount(params.get("amount"));

        Category category = resolveCategory(userId, categoryName);

        // Budgets are per-month; the Coach always operates on the current month.
        String month = YearMonth.now().toString();
        createBudgetUseCase.execute(userId, category.getId(), amount, month);

        String summary = "Budget for " + category.getName() + " created for the current month.";
        return Response.ok(Map.of("status", "done", "summary", summary)).build();
    }

    private Response executeCreateGoal(UUID userId, Map<String, Object> params) {
        String title = requireString(params.get("title"), "title");
        BigDecimal target = requirePositiveAmount(params.get("target_amount"));
        LocalDate deadline = parseOptionalDate(params.get("deadline"));

        createGoalUseCase.execute(userId, title, target, deadline);

        return Response.ok(Map.of("status", "done", "summary", "Goal \"" + title + "\" created.")).build();
    }

    private Response executeAdjustBudget(UUID userId, Map<String, Object> params) {
        String categoryName = requireString(params.get("category"), "category");
        BigDecimal amount = requirePositiveAmount(params.get("amount"));

        Category category = resolveCategory(userId, categoryName);
        String month = YearMonth.now().toString();
        Budget budget = listBudgetsUseCase.execute(userId, month).stream()
                .filter(b -> b.getCategoryId().equals(category.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "No " + category.getName() + " budget this month to adjust."));

        updateBudgetUseCase.execute(userId, budget.getId(), amount);

        return Response.ok(Map.of(
                "status", "done",
                "summary", "Budget for " + category.getName() + " adjusted for the current month.")).build();
    }

    private Response executeLogTransaction(UUID userId, Map<String, Object> params) {
        TransactionType type = parseType(params.get("type"));
        String categoryName = requireString(params.get("category"), "category");
        BigDecimal amount = requirePositiveAmount(params.get("amount"));
        Object descObj = params.get("description");
        String description = (descObj == null || descObj.toString().isBlank()) ? null : descObj.toString().trim();

        // Resolve the category among the user's own categories of the matching
        // type, so an EXPENSE can't be logged against an INCOME category.
        Category category = categoryRepository.findByUserIdAndType(userId, type).stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Category '" + categoryName + "' not found for " + typeLabel(type) + "."));

        createTransactionUseCase.execute(userId, category.getId(), type, amount, description, LocalDate.now());

        return Response.ok(Map.of(
                "status", "done",
                "summary", typeLabel(type) + " logged in " + category.getName() + ".")).build();
    }

    private Category resolveCategory(UUID userId, String name) {
        return categoryRepository.findByUserId(userId).stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new DomainException("Category not found: " + name));
    }

    private Response executeContributeGoal(UUID userId, Map<String, Object> params) {
        String goalTitle = requireString(params.get("goal_title"), "goal_title");
        BigDecimal amount = requirePositiveAmount(params.get("amount"));

        // Resolve the goal among the authenticated user's own goals, so the
        // contribute use-case (which takes a goalId, not a userId) can never
        // touch another account's goal.
        Goal goal = listGoalsUseCase.execute(userId).stream()
                .filter(g -> g.getTitle().toLowerCase().contains(goalTitle.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Goal not found: " + goalTitle));

        contributeToGoalUseCase.execute(goal.getId(), amount, LocalDate.now());

        String summary = "Contribution recorded for goal " + goal.getTitle() + ".";
        return Response.ok(Map.of("status", "done", "summary", summary)).build();
    }

    private static String requireString(Object value, String field) {
        if (value == null || value.toString().isBlank()) {
            throw new DomainException("Parâmetro '" + field + "' é obrigatório.");
        }
        return value.toString().trim();
    }

    private static BigDecimal requirePositiveAmount(Object value) {
        if (value == null) {
            throw new DomainException("Valor é obrigatório.");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new DomainException("Valor inválido: " + value);
        }
        if (amount.signum() <= 0) {
            throw new DomainException("O valor deve ser positivo.");
        }
        return amount;
    }

    private static LocalDate parseOptionalDate(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.toString().trim());
        } catch (DateTimeParseException e) {
            throw new DomainException("Data inválida: " + value + " (use AAAA-MM-DD).");
        }
    }

    private static TransactionType parseType(Object value) {
        String raw = requireString(value, "type").toUpperCase();
        try {
            return TransactionType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Tipo inválido: " + value + " (use INCOME ou EXPENSE).");
        }
    }

    private static String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Income" : "Expense";
    }
}
