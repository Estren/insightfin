import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/presentation/bloc/auth/auth_bloc.dart';
import 'package:orizon/src/presentation/bloc/auth/auth_event.dart';
import 'package:orizon/src/presentation/bloc/auth/auth_state.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_bloc.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_state.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_bloc.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_state.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_bloc.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_state.dart';
import 'package:orizon/src/presentation/widget/summary_card.dart';
import 'package:orizon/src/presentation/widget/transaction_card.dart';

class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final monthLabel = DateFormat('MMMM yyyy').format(DateTime.now());

    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is AuthUnauthenticated) {
          context.go('/login');
        }
      },
      child: Scaffold(
      appBar: AppBar(
        title: Text(l10n.appTitle),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              context.read<AuthBloc>().add(AuthLogoutRequested());
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.sm,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Month header
            Text(
              monthLabel,
              style: AppTypography.bodySmall.copyWith(
                color: AppColors.neutral500,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: AppSpacing.sm),

            // Summary cards
            _BalanceSummary(),
            const SizedBox(height: AppSpacing.lg),

            // Recent transactions
            _SectionTitle(
              title: l10n.transactions,
              onSeeAll: () => context.go('/transactions'),
            ),
            const SizedBox(height: AppSpacing.sm),
            _RecentTransactions(),
            const SizedBox(height: AppSpacing.lg),

            // Active goals
            _SectionTitle(
              title: l10n.goals,
              onSeeAll: () => context.go('/goals'),
            ),
            const SizedBox(height: AppSpacing.sm),
            _ActiveGoals(),
            const SizedBox(height: AppSpacing.lg),

            // Budget overview
            _SectionTitle(
              title: l10n.budgets,
              onSeeAll: () => context.go('/budgets'),
            ),
            const SizedBox(height: AppSpacing.sm),
            _BudgetOverview(),
            const SizedBox(height: AppSpacing.xl),
          ],
        ),
      ),
      bottomNavigationBar: NavigationBar(
        destinations: [
          NavigationDestination(
            icon: const Icon(Icons.dashboard_outlined),
            selectedIcon: const Icon(Icons.dashboard),
            label: l10n.dashboard,
          ),
          NavigationDestination(
            icon: const Icon(Icons.receipt_long_outlined),
            selectedIcon: const Icon(Icons.receipt_long),
            label: l10n.transactions,
          ),
          NavigationDestination(
            icon: const Icon(Icons.flag_outlined),
            selectedIcon: const Icon(Icons.flag),
            label: l10n.goals,
          ),
          NavigationDestination(
            icon: const Icon(Icons.account_balance_wallet_outlined),
            selectedIcon: const Icon(Icons.account_balance_wallet),
            label: l10n.budgets,
          ),
        ],
        selectedIndex: 0,
        onDestinationSelected: (index) {
          switch (index) {
            case 1:
              context.go('/transactions');
              break;
            case 2:
              context.go('/goals');
              break;
            case 3:
              context.go('/budgets');
              break;
          }
        },
      ),
    ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  final String title;
  final VoidCallback onSeeAll;

  const _SectionTitle({required this.title, required this.onSeeAll});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(title, style: AppTypography.h3),
        TextButton(
          onPressed: onSeeAll,
          child: Text(
            'See all',
            style: AppTypography.bodySmall.copyWith(
              color: AppColors.primary600,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ],
    );
  }
}

class _BalanceSummary extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return BlocBuilder<TransactionBloc, TransactionState>(
      builder: (context, state) {
        double income = 0;
        double expense = 0;

        if (state is TransactionLoaded) {
          income = state.totalIncome;
          expense = state.totalExpense;
        }

        final balance = income - expense;

        return Column(
          children: [
            // Balance card with gradient
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(AppSpacing.lg),
              decoration: BoxDecoration(
                gradient: AppColors.horizonGradient,
                borderRadius: AppSpacing.radiusMd,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    l10n.balance,
                    style: AppTypography.bodySmall.copyWith(
                      color: Colors.white.withOpacity(0.8),
                    ),
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$')
                        .format(balance),
                    style: AppTypography.display.copyWith(
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.sm),
            Row(
              children: [
                Expanded(
                  child: SummaryCard(
                    label: l10n.totalIncome,
                    value: income,
                    icon: Icons.arrow_upward,
                    valueColor: AppColors.income,
                    iconBackgroundColor: AppColors.income.withOpacity(0.1),
                  ),
                ),
                const SizedBox(width: AppSpacing.sm),
                Expanded(
                  child: SummaryCard(
                    label: l10n.totalExpenses,
                    value: expense,
                    icon: Icons.arrow_downward,
                    valueColor: AppColors.expense,
                    iconBackgroundColor: AppColors.expense.withOpacity(0.1),
                  ),
                ),
              ],
            ),
          ],
        );
      },
    );
  }
}

class _RecentTransactions extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return BlocBuilder<TransactionBloc, TransactionState>(
      builder: (context, state) {
        if (state is TransactionLoading) {
          return const Center(
            child: Padding(
              padding: EdgeInsets.all(AppSpacing.lg),
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          );
        }

        if (state is TransactionLoaded) {
          if (state.transactions.isEmpty) {
            return Card(
              child: Padding(
                padding: AppSpacing.paddingCard,
                child: Center(
                  child: Text(
                    l10n.noTransactions,
                    style: AppTypography.body.copyWith(
                      color: AppColors.neutral500,
                    ),
                  ),
                ),
              ),
            );
          }

          final recent = state.transactions.take(5).toList();
          return Column(
            children: recent
                .map((t) => Padding(
                      padding:
                          const EdgeInsets.only(bottom: AppSpacing.xs),
                      child: TransactionCard(
                        transaction: t,
                        categoryName: t.categoryName,
                      ),
                    ))
                .toList(),
          );
        }

        return const SizedBox.shrink();
      },
    );
  }
}

class _ActiveGoals extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return BlocBuilder<GoalBloc, GoalState>(
      builder: (context, state) {
        if (state is GoalLoading) {
          return const Center(
            child: Padding(
              padding: EdgeInsets.all(AppSpacing.lg),
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          );
        }

        if (state is GoalLoaded) {
          final active = state.activeGoals;
          if (active.isEmpty) {
            return Card(
              child: Padding(
                padding: AppSpacing.paddingCard,
                child: Center(
                  child: Text(
                    l10n.noGoals,
                    style: AppTypography.body.copyWith(
                      color: AppColors.neutral500,
                    ),
                  ),
                ),
              ),
            );
          }

          return Column(
            children: active.take(3).map((goal) {
              final formatter =
                  NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');
              final percentage =
                  (goal.progressPercentage * 100).toStringAsFixed(0);

              return Padding(
                padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                child: Card(
                  child: InkWell(
                    onTap: () => context.push(
                      '/goals/${goal.id}/contribute',
                      extra: goal,
                    ),
                    borderRadius: AppSpacing.radiusMd,
                    child: Padding(
                      padding: const EdgeInsets.all(AppSpacing.sm + 4),
                      child: Row(
                        children: [
                          SizedBox(
                            width: 44,
                            height: 44,
                            child: Stack(
                              alignment: Alignment.center,
                              children: [
                                CircularProgressIndicator(
                                  value: goal.progressPercentage,
                                  backgroundColor: AppColors.neutral200,
                                  color: _goalColor(goal),
                                  strokeWidth: 4,
                                ),
                                Text(
                                  '$percentage%',
                                  style: AppTypography.bodySmall.copyWith(
                                    fontSize: 10,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(width: AppSpacing.sm + 4),
                          Expanded(
                            child: Column(
                              crossAxisAlignment:
                                  CrossAxisAlignment.start,
                              children: [
                                Text(goal.title,
                                    style: AppTypography.label),
                                Text(
                                  '${formatter.format(goal.currentAmount)} / ${formatter.format(goal.targetAmount)}',
                                  style:
                                      AppTypography.bodySmall.copyWith(
                                    color: AppColors.neutral500,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Icon(Icons.chevron_right,
                              color: AppColors.neutral400, size: 20),
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }).toList(),
          );
        }

        return const SizedBox.shrink();
      },
    );
  }

  Color _goalColor(GoalEntity goal) {
    if (goal.progressPercentage >= 0.75) return AppColors.income;
    if (goal.progressPercentage >= 0.4) return AppColors.accent500;
    return AppColors.primary500;
  }
}

class _BudgetOverview extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return BlocBuilder<BudgetBloc, BudgetState>(
      builder: (context, state) {
        if (state is BudgetLoading) {
          return const Center(
            child: Padding(
              padding: EdgeInsets.all(AppSpacing.lg),
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          );
        }

        if (state is BudgetLoaded) {
          if (state.budgets.isEmpty) {
            return Card(
              child: Padding(
                padding: AppSpacing.paddingCard,
                child: Center(
                  child: Text(
                    l10n.noBudgets,
                    style: AppTypography.body.copyWith(
                      color: AppColors.neutral500,
                    ),
                  ),
                ),
              ),
            );
          }

          final formatter =
              NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');

          return Column(
            children: state.budgets.take(3).map((budget) {
              return Padding(
                padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(AppSpacing.sm + 4),
                    child: Row(
                      children: [
                        Container(
                          width: 40,
                          height: 40,
                          decoration: BoxDecoration(
                            color: AppColors.primary100,
                            borderRadius: AppSpacing.radiusSm,
                          ),
                          child: const Icon(
                            Icons.account_balance_wallet_outlined,
                            color: AppColors.primary600,
                            size: 20,
                          ),
                        ),
                        const SizedBox(width: AppSpacing.sm + 4),
                        Expanded(
                          child: Column(
                            crossAxisAlignment:
                                CrossAxisAlignment.start,
                            children: [
                              Text(budget.categoryName,
                                  style: AppTypography.label),
                              Text(
                                'Limit: ${formatter.format(budget.amount)}',
                                style:
                                    AppTypography.bodySmall.copyWith(
                                  color: AppColors.neutral500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            }).toList(),
          );
        }

        return const SizedBox.shrink();
      },
    );
  }
}
