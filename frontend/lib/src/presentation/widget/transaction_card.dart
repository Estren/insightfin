import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

class TransactionCard extends StatelessWidget {
  final TransactionEntity transaction;
  final String categoryName;
  final IconData categoryIcon;
  final VoidCallback? onTap;

  const TransactionCard({
    super.key,
    required this.transaction,
    required this.categoryName,
    this.categoryIcon = Icons.category_outlined,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final isExpense = transaction.type == TransactionType.expense;
    final amountColor = isExpense ? AppColors.expense : AppColors.income;
    final sign = isExpense ? '-' : '+';
    final formatter = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');
    final dateFormat = DateFormat('dd MMM', 'pt_BR');

    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: AppSpacing.radiusMd,
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.sm + 4,
          ),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: amountColor.withOpacity(0.1),
                  borderRadius: AppSpacing.radiusSm,
                ),
                child: Icon(categoryIcon, color: amountColor, size: 22),
              ),
              const SizedBox(width: AppSpacing.sm + 4),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      transaction.description ?? categoryName,
                      style: AppTypography.label,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '$categoryName · ${dateFormat.format(transaction.date)}',
                      style: AppTypography.bodySmall.copyWith(
                        color: AppColors.neutral500,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              Text(
                '$sign ${formatter.format(transaction.amount)}',
                style: AppTypography.amountSmall.copyWith(color: amountColor),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
