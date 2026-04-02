package com.orizon.coreapi.config;

import com.orizon.coreapi.application.service.*;
import com.orizon.coreapi.domain.port.out.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class BeanConfig {

    @Produces
    @ApplicationScoped
    public UserService userService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   TokenProvider tokenProvider) {
        return new UserService(userRepository, passwordEncoder, tokenProvider);
    }

    @Produces
    @ApplicationScoped
    public TransactionService transactionService(TransactionRepository transactionRepository,
                                                 CategoryRepository categoryRepository) {
        return new TransactionService(transactionRepository, categoryRepository);
    }

    @Produces
    @ApplicationScoped
    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryService(categoryRepository);
    }

    @Produces
    @ApplicationScoped
    public GoalService goalService(GoalRepository goalRepository,
                                   GoalContributionRepository goalContributionRepository) {
        return new GoalService(goalRepository, goalContributionRepository);
    }

    @Produces
    @ApplicationScoped
    public BudgetService budgetService(BudgetRepository budgetRepository,
                                       CategoryRepository categoryRepository) {
        return new BudgetService(budgetRepository, categoryRepository);
    }
}
