import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_bloc.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_event.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_state.dart';
import 'package:orizon/src/presentation/widget/empty_state.dart';
import 'package:orizon/src/presentation/widget/loading_indicator.dart';
import 'package:orizon/src/presentation/widget/transaction_card.dart';

class TransactionsPage extends StatelessWidget {
  const TransactionsPage({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.transactions),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(48),
          child: _MonthSelector(),
        ),
      ),
      body: BlocBuilder<TransactionBloc, TransactionState>(
        builder: (context, state) {
          if (state is TransactionLoading) {
            return const LoadingIndicator();
          }

          if (state is TransactionError) {
            return EmptyState(
              icon: Icons.error_outline,
              title: state.message,
            );
          }

          if (state is TransactionLoaded) {
            if (state.transactions.isEmpty) {
              return EmptyState(
                icon: Icons.receipt_long_outlined,
                title: l10n.noTransactions,
                actionLabel: l10n.addTransaction,
                onAction: () => context.push('/transactions/create'),
              );
            }

            final grouped = state.groupedByDate;

            return RefreshIndicator(
              onRefresh: () async {
                final month = state.selectedMonth;
                context.read<TransactionBloc>().add(
                      TransactionsLoadRequested(
                        startDate: DateTime(month.year, month.month, 1),
                        endDate:
                            DateTime(month.year, month.month + 1, 0),
                      ),
                    );
              },
              child: ListView.builder(
                padding: const EdgeInsets.only(
                  left: AppSpacing.md,
                  right: AppSpacing.md,
                  top: AppSpacing.sm,
                  bottom: 80,
                ),
                itemCount: grouped.length,
                itemBuilder: (context, index) {
                  final dateKey = grouped.keys.elementAt(index);
                  final transactions = grouped[dateKey]!;
                  final date = DateTime.parse(dateKey);
                  final dateLabel = DateFormat('dd MMM, EEEE').format(date);

                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (index > 0)
                        const SizedBox(height: AppSpacing.md),
                      Padding(
                        padding: const EdgeInsets.symmetric(
                          vertical: AppSpacing.xs,
                          horizontal: AppSpacing.xs,
                        ),
                        child: Text(
                          dateLabel,
                          style: AppTypography.bodySmall.copyWith(
                            color: AppColors.neutral500,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      ...transactions.map(
                        (t) => Padding(
                          padding: const EdgeInsets.only(
                              bottom: AppSpacing.xs),
                          child: TransactionCard(
                            transaction: t,
                            categoryName: t.categoryId,
                          ),
                        ),
                      ),
                    ],
                  );
                },
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/transactions/create'),
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _MonthSelector extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<TransactionBloc, TransactionState>(
      buildWhen: (prev, curr) => curr is TransactionLoaded,
      builder: (context, state) {
        final now = DateTime.now();
        final selectedMonth =
            state is TransactionLoaded ? state.selectedMonth : now;
        final label = DateFormat('MMMM yyyy').format(selectedMonth);

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
                  final prev = DateTime(
                      selectedMonth.year, selectedMonth.month - 1, 1);
                  context
                      .read<TransactionBloc>()
                      .add(TransactionMonthChanged(prev));
                },
                visualDensity: VisualDensity.compact,
              ),
              Text(
                label,
                style: AppTypography.label.copyWith(
                  color: AppColors.neutral700,
                ),
              ),
              IconButton(
                icon: const Icon(Icons.chevron_right, size: 20),
                onPressed: () {
                  final next = DateTime(
                      selectedMonth.year, selectedMonth.month + 1, 1);
                  if (!next.isAfter(
                      DateTime(now.year, now.month, now.day))) {
                    context
                        .read<TransactionBloc>()
                        .add(TransactionMonthChanged(next));
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
