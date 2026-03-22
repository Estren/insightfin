package com.orizon.coreapi.config;

import com.orizon.coreapi.application.service.*;
import com.orizon.coreapi.domain.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UserService userService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   TokenProvider tokenProvider) {
        return new UserService(userRepository, passwordEncoder, tokenProvider);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository,
                                                 CategoryRepository categoryRepository) {
        return new TransactionService(transactionRepository, categoryRepository);
    }

    @Bean
    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryService(categoryRepository);
    }

    @Bean
    public GoalService goalService(GoalRepository goalRepository,
                                   GoalContributionRepository goalContributionRepository) {
        return new GoalService(goalRepository, goalContributionRepository);
    }

    @Bean
    public BudgetService budgetService(BudgetRepository budgetRepository,
                                       CategoryRepository categoryRepository) {
        return new BudgetService(budgetRepository, categoryRepository);
    }
}
