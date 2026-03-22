import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_bloc.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_event.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_state.dart';
import 'package:orizon/src/presentation/widget/budget_card.dart';
import 'package:orizon/src/presentation/widget/empty_state.dart';
import 'package:orizon/src/presentation/widget/loading_indicator.dart';

class BudgetsPage extends StatelessWidget {
  const BudgetsPage({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.budgets),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(48),
          child: _MonthSelector(),
        ),
      ),
      body: BlocBuilder<BudgetBloc, BudgetState>(
        builder: (context, state) {
          if (state is BudgetLoading) {
            return const LoadingIndicator();
          }

          if (state is BudgetError) {
            return EmptyState(
              icon: Icons.error_outline,
              title: state.message,
            );
          }

          if (state is BudgetLoaded) {
            if (state.budgets.isEmpty) {
              return EmptyState(
                icon: Icons.account_balance_wallet_outlined,
                title: l10n.noBudgets,
                actionLabel: l10n.addBudget,
                onAction: () => context.push('/budgets/create'),
              );
            }

            final formatter =
                NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');

            return RefreshIndicator(
              onRefresh: () async {
                context.read<BudgetBloc>().add(
                      BudgetsLoadRequested(month: state.selectedMonth),
                    );
              },
              child: ListView(
                padding: const EdgeInsets.all(AppSpacing.md),
                children: [
                  // Total budgeted header
                  Container(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    decoration: BoxDecoration(
                      color: AppColors.primary50,
                      borderRadius: AppSpacing.radiusMd,
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Total budgeted',
                          style: AppTypography.label
                              .copyWith(color: AppColors.primary700),
                        ),
                        Text(
                          formatter.format(state.totalBudgeted),
                          style: AppTypography.amount
                              .copyWith(color: AppColors.primary700),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppSpacing.md),
                  ...state.budgets.map((budget) => Padding(
                        padding:
                            const EdgeInsets.only(bottom: AppSpacing.sm),
                        child: BudgetCard(
                          budget: budget,
                          categoryName: budget.categoryId,
                          spent: 0,
                        ),
                      )),
                ],
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/budgets/create'),
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _MonthSelector extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<BudgetBloc, BudgetState>(
      buildWhen: (prev, curr) => curr is BudgetLoaded,
      builder: (context, state) {
        final now = DateTime.now();
        final currentMonth = state is BudgetLoaded
            ? state.selectedMonth
            : '${now.year}-${now.month.toString().padLeft(2, '0')}';

        final parts = currentMonth.split('-');
        final year = int.parse(parts[0]);
        final month = int.parse(parts[1]);
        final date = DateTime(year, month);
        final label = DateFormat('MMMM yyyy').format(date);

        return Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.xs,
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(
                icon: const Icon(Icons.chevron_left, size: 20),
                onPressed: () {
                  final prev = DateTime(year, month - 1);
                  final monthStr =
                      '${prev.year}-${prev.month.toString().padLeft(2, '0')}';
                  context
                      .read<BudgetBloc>()
                      .add(BudgetMonthChanged(monthStr));
                },
                visualDensity: VisualDensity.compact,
              ),
              Text(
                label,
                style: AppTypography.label
                    .copyWith(color: AppColors.neutral700),
              ),
              IconButton(
                icon: const Icon(Icons.chevron_right, size: 20),
                onPressed: () {
                  final next = DateTime(year, month + 1);
                  if (!next.isAfter(DateTime(now.year, now.month))) {
                    final monthStr =
                        '${next.year}-${next.month.toString().padLeft(2, '0')}';
                    context
                        .read<BudgetBloc>()
                        .add(BudgetMonthChanged(monthStr));
                  }
                },
                visualDensity: VisualDensity.compact,
              ),
            ],
          ),
        );
      },
    );
  }
}
