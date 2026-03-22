import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';

class GoalCard extends StatelessWidget {
  final GoalEntity goal;
  final VoidCallback? onTap;

  const GoalCard({
    super.key,
    required this.goal,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final formatter = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');
    final percentage = (goal.progressPercentage * 100).toStringAsFixed(0);
    final progressColor = _progressColor();

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
                      color: AppColors.primary100,
                      borderRadius: AppSpacing.radiusSm,
                    ),
                    child: const Icon(
                      Icons.flag_outlined,
                      color: AppColors.primary600,
                      size: 20,
                    ),
                  ),
                  const SizedBox(width: AppSpacing.sm + 4),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          goal.title,
                          style: AppTypography.label,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        if (goal.deadline != null)
                          Text(
                            DateFormat('dd MMM yyyy').format(goal.deadline!),
                            style: AppTypography.bodySmall.copyWith(
                              color: AppColors.neutral500,
                            ),
                          ),
                      ],
                    ),
                  ),
                  _StatusChip(status: goal.status),
                ],
              ),
              const SizedBox(height: AppSpacing.md),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    formatter.format(goal.currentAmount),
                    style: AppTypography.amountSmall.copyWith(
                      color: progressColor,
                    ),
                  ),
                  Text(
                    formatter.format(goal.targetAmount),
                    style: AppTypography.bodySmall.copyWith(
                      color: AppColors.neutral500,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.xs),
              ClipRRect(
                borderRadius: AppSpacing.radiusFull,
                child: LinearProgressIndicator(
                  value: goal.progressPercentage,
                  backgroundColor: AppColors.neutral200,
                  color: progressColor,
                  minHeight: 8,
                ),
              ),
              const SizedBox(height: AppSpacing.xs),
              Align(
                alignment: Alignment.centerRight,
                child: Text(
                  '$percentage%',
                  style: AppTypography.bodySmall.copyWith(
                    color: AppColors.neutral500,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _progressColor() {
    if (goal.status == GoalStatus.completed) return AppColors.income;
    if (goal.status == GoalStatus.cancelled) return AppColors.neutral400;
    if (goal.progressPercentage >= 0.75) return AppColors.income;
    if (goal.progressPercentage >= 0.4) return AppColors.accent500;
    return AppColors.primary500;
  }
}

class _StatusChip extends StatelessWidget {
  final GoalStatus status;

  const _StatusChip({required this.status});

  @override
  Widget build(BuildContext context) {
    final (label, bgColor, fgColor) = switch (status) {
      GoalStatus.active => ('Active', AppColors.primary50, AppColors.primary700),
      GoalStatus.completed => ('Done', const Color(0xFFECFDF5), AppColors.income),
      GoalStatus.cancelled => ('Cancelled', AppColors.neutral100, AppColors.neutral500),
    };

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: AppSpacing.radiusFull,
      ),
      child: Text(
        label,
        style: AppTypography.bodySmall.copyWith(
          color: fgColor,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}
