import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/budget/budget_status_entity.dart';

class BudgetCard extends StatelessWidget {
  final BudgetStatusEntity status;
  final IconData categoryIcon;
  final VoidCallback? onTap;

  const BudgetCard({
    super.key,
    required this.status,
    this.categoryIcon = Icons.account_balance_wallet_outlined,
    this.onTap,
  });

  double get _usage => status.budgetAmount > 0
      ? (status.spentAmount / status.budgetAmount).clamp(0.0, 1.0)
      : 0.0;

  @override
  Widget build(BuildContext context) {
    final formatter = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');
    final percentage = (_usage * 100).toStringAsFixed(0);
    final barColor = _usageColor();

    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: AppSpacing.radiusMd,
        child: Padding(
          padding: AppSpacing.paddingCard,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: barColor.withOpacity(0.1),
                      borderRadius: AppSpacing.radiusSm,
                    ),
                    child: Icon(categoryIcon, color: barColor, size: 20),
                  ),
                  const SizedBox(width: AppSpacing.sm + 4),
                  Expanded(
                    child: Text(
                      status.categoryName,
                      style: AppTypography.label,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  Text(
                    '$percentage%',
                    style: AppTypography.bodySmall.copyWith(
                      color: barColor,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.sm + 4),
              ClipRRect(
                borderRadius: AppSpacing.radiusFull,
                child: LinearProgressIndicator(
                  value: _usage,
                  backgroundColor: AppColors.neutral200,
                  color: barColor,
                  minHeight: 8,
                ),
              ),
              const SizedBox(height: AppSpacing.xs),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Spent ${formatter.format(status.spentAmount)}',
                    style: AppTypography.bodySmall.copyWith(
                      color: AppColors.neutral500,
                    ),
                  ),
                  Text(
                    'of ${formatter.format(status.budgetAmount)}',
                    style: AppTypography.bodySmall.copyWith(
                      color: AppColors.neutral500,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _usageColor() {
    if (_usage >= 0.9) return AppColors.expense;
    if (_usage >= 0.7) return AppColors.warning;
    return AppColors.income;
  }
}
