import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_event.dart';
import 'package:orizon/src/presentation/bloc/category/category_state.dart';
import 'package:orizon/src/presentation/widget/empty_state.dart';
import 'package:orizon/src/presentation/widget/loading_indicator.dart';

class CategoriesPage extends StatelessWidget {
  const CategoriesPage({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.categories)),
      body: Column(
        children: [
          _FilterChips(),
          Expanded(
            child: BlocBuilder<CategoryBloc, CategoryState>(
              builder: (context, state) {
                if (state is CategoryLoading) {
                  return const LoadingIndicator();
                }

                if (state is CategoryError) {
                  return EmptyState(
                    icon: Icons.error_outline,
                    title: state.message,
                  );
                }

                if (state is CategoryLoaded) {
                  if (state.categories.isEmpty) {
                    return EmptyState(
                      icon: Icons.category_outlined,
                      title: l10n.noCategories,
                      actionLabel: l10n.addCategory,
                      onAction: () => context.push('/categories/create'),
                    );
                  }

                  return RefreshIndicator(
                    onRefresh: () async {
                      context.read<CategoryBloc>().add(
                            CategoriesLoadRequested(type: state.filterType),
                          );
                    },
                    child: ListView.separated(
                      padding: const EdgeInsets.all(AppSpacing.md),
                      itemCount: state.categories.length,
                      separatorBuilder: (_, __) =>
                          const SizedBox(height: AppSpacing.sm),
                      itemBuilder: (context, index) {
                        final category = state.categories[index];
                        final isExpense =
                            category.type == TransactionType.expense;
                        final color =
                            isExpense ? AppColors.expense : AppColors.income;

                        return Card(
                          child: ListTile(
                            leading: Container(
                              width: 40,
                              height: 40,
                              decoration: BoxDecoration(
                                color: color.withOpacity(0.1),
                                borderRadius: AppSpacing.radiusSm,
                              ),
                              child: Icon(
                                _iconFromString(category.icon),
                                color: color,
                                size: 20,
                              ),
                            ),
                            title: Text(
                              category.name,
                              style: AppTypography.label,
                            ),
                            subtitle: Text(
                              isExpense ? l10n.expense : l10n.income,
                              style: AppTypography.bodySmall.copyWith(
                                color: AppColors.neutral500,
                              ),
                            ),
                            trailing: Icon(
                              Icons.chevron_right,
                              color: AppColors.neutral400,
                            ),
                          ),
                        );
                      },
                    ),
                  );
                }

                return const SizedBox.shrink();
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/categories/create'),
        child: const Icon(Icons.add),
      ),
    );
  }

  IconData _iconFromString(String? iconName) {
    const iconMap = {
      'food': Icons.restaurant_outlined,
      'transport': Icons.directions_car_outlined,
      'shopping': Icons.shopping_bag_outlined,
      'health': Icons.favorite_outline,
      'education': Icons.school_outlined,
      'entertainment': Icons.movie_outlined,
      'housing': Icons.home_outlined,
      'salary': Icons.account_balance_outlined,
      'investment': Icons.trending_up_outlined,
      'gift': Icons.card_giftcard_outlined,
      'other': Icons.more_horiz_outlined,
    };
    return iconMap[iconName] ?? Icons.category_outlined;
  }
}

class _FilterChips extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return BlocBuilder<CategoryBloc, CategoryState>(
      buildWhen: (prev, curr) => curr is CategoryLoaded,
      builder: (context, state) {
        final currentFilter =
            state is CategoryLoaded ? state.filterType : null;

        return Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.sm,
          ),
          child: Row(
            children: [
              _buildChip(
                context,
                label: l10n.allTypes,
                selected: currentFilter == null,
                onSelected: () => context
                    .read<CategoryBloc>()
                    .add(const CategoryFilterChanged()),
              ),
              const SizedBox(width: AppSpacing.sm),
              _buildChip(
                context,
                label: l10n.expense,
                selected: currentFilter == TransactionType.expense,
                onSelected: () => context.read<CategoryBloc>().add(
                    const CategoryFilterChanged(
                        type: TransactionType.expense)),
              ),
              const SizedBox(width: AppSpacing.sm),
              _buildChip(
                context,
                label: l10n.income,
                selected: currentFilter == TransactionType.income,
                onSelected: () => context.read<CategoryBloc>().add(
                    const CategoryFilterChanged(
                        type: TransactionType.income)),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildChip(
    BuildContext context, {
    required String label,
    required bool selected,
    required VoidCallback onSelected,
  }) {
    return FilterChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) => onSelected(),
      selectedColor: AppColors.primary100,
      checkmarkColor: AppColors.primary700,
      labelStyle: AppTypography.bodySmall.copyWith(
        color: selected ? AppColors.primary700 : AppColors.neutral600,
        fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
      ),
    );
  }
}
