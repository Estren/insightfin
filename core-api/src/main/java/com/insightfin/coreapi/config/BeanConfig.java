package com.insightfin.coreapi.config;

import com.insightfin.coreapi.application.service.*;
import com.insightfin.coreapi.domain.port.out.*;
import com.insightfin.coreapi.domain.port.out.AvatarStoragePort;
import com.insightfin.coreapi.domain.port.out.EventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class BeanConfig {

    @ConfigProperty(name = "app.password-reset.token-ttl-minutes", defaultValue = "30")
    int passwordResetTtlMinutes;

    @ConfigProperty(name = "app.frontend.base-url", defaultValue = "http://localhost:4200")
    String frontendBaseUrl;

    @ConfigProperty(name = "app.email-verification.required", defaultValue = "true")
    boolean emailVerificationRequired;

    @ConfigProperty(name = "app.email-verification.registration-ttl-hours", defaultValue = "24")
    int emailVerificationTtlHours;

    @ConfigProperty(name = "app.email-verification.pin-max-attempts", defaultValue = "5")
    int emailVerificationPinMaxAttempts;

    @ConfigProperty(name = "app.account-lockout.max-attempts", defaultValue = "5")
    int accountLockoutMaxAttempts;

    @ConfigProperty(name = "app.account-lockout.duration-minutes", defaultValue = "15")
    int accountLockoutDurationMinutes;

    @Produces
    @ApplicationScoped
    public UserService userService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   TokenProvider tokenProvider,
                                   RefreshTokenRepository refreshTokenRepository,
                                   AvatarStoragePort avatarStoragePort,
                                   GoogleTokenVerifier googleTokenVerifier,
                                   EmailVerificationService emailVerificationService) {
        return new UserService(userRepository, passwordEncoder, tokenProvider,
                refreshTokenRepository, avatarStoragePort, googleTokenVerifier,
                emailVerificationService, emailVerificationRequired,
                accountLockoutMaxAttempts, accountLockoutDurationMinutes);
    }

    @Produces
    @ApplicationScoped
    public EmailVerificationService emailVerificationService(UserRepository userRepository,
                                                             EmailVerificationTokenRepository tokenRepository,
                                                             EmailSender emailSender,
                                                             RefreshTokenRepository refreshTokenRepository) {
        return new EmailVerificationService(userRepository, tokenRepository, emailSender,
                refreshTokenRepository, emailVerificationTtlHours, emailVerificationPinMaxAttempts, frontendBaseUrl);
    }

    @Produces
    @ApplicationScoped
    public TransactionService transactionService(TransactionRepository transactionRepository,
                                                 CategoryRepository categoryRepository,
                                                 EventPublisher eventPublisher) {
        return new TransactionService(transactionRepository, categoryRepository, eventPublisher);
    }

    @Produces
    @ApplicationScoped
    public CategoryService categoryService(CategoryRepository categoryRepository,
                                           RecurringTransactionRepository recurringTransactionRepository) {
        return new CategoryService(categoryRepository, recurringTransactionRepository);
    }

    @Produces
    @ApplicationScoped
    public RecurringTransactionService recurringTransactionService(
            RecurringTransactionRepository recurringRepository,
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            EventPublisher eventPublisher) {
        return new RecurringTransactionService(recurringRepository, transactionRepository,
                categoryRepository, eventPublisher);
    }

    @Produces
    @ApplicationScoped
    public GoalService goalService(GoalRepository goalRepository,
                                   GoalContributionRepository goalContributionRepository,
                                   EventPublisher eventPublisher) {
        return new GoalService(goalRepository, goalContributionRepository, eventPublisher);
    }

    @Produces
    @ApplicationScoped
    public BudgetService budgetService(BudgetRepository budgetRepository,
                                       CategoryRepository categoryRepository,
                                       TransactionRepository transactionRepository) {
        return new BudgetService(budgetRepository, categoryRepository, transactionRepository);
    }

    @Produces
    @ApplicationScoped
    public DashboardService dashboardService(TransactionRepository transactionRepository,
                                             GoalRepository goalRepository,
                                             BudgetService budgetService) {
        return new DashboardService(transactionRepository, goalRepository, budgetService);
    }

    @Produces
    @ApplicationScoped
    public AiFeedbackService aiFeedbackService(AiFeedbackRepository aiFeedbackRepository) {
        return new AiFeedbackService(aiFeedbackRepository);
    }

    @Produces
    @ApplicationScoped
    public BudgetAlertService budgetAlertService(BudgetAlertRepository budgetAlertRepository,
                                                 BudgetRepository budgetRepository,
                                                 TransactionRepository transactionRepository) {
        return new BudgetAlertService(budgetAlertRepository, budgetRepository, transactionRepository);
    }

    @Produces
    @ApplicationScoped
    public NotificationService notificationService(AiFeedbackRepository aiFeedbackRepository,
                                                   BudgetAlertRepository budgetAlertRepository,
                                                   BudgetRepository budgetRepository,
                                                   CategoryRepository categoryRepository) {
        return new NotificationService(aiFeedbackRepository, budgetAlertRepository,
                budgetRepository, categoryRepository);
    }

    @Produces
    @ApplicationScoped
    public PasswordResetService passwordResetService(UserRepository userRepository,
                                                     PasswordResetTokenRepository tokenRepository,
                                                     PasswordEncoder passwordEncoder,
                                                     RefreshTokenRepository refreshTokenRepository,
                                                     EmailSender emailSender) {
        return new PasswordResetService(userRepository, tokenRepository, passwordEncoder,
                refreshTokenRepository, emailSender, passwordResetTtlMinutes, frontendBaseUrl);
    }
}
