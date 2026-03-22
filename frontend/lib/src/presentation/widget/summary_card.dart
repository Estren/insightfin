import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';

class SummaryCard extends StatelessWidget {
  final String label;
  final double value;
  final IconData icon;
  final Color? valueColor;
  final Color? iconBackgroundColor;

  const SummaryCard({
    super.key,
    required this.label,
    required this.value,
    required this.icon,
    this.valueColor,
    this.iconBackgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    final formatter = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');
    final color = valueColor ?? Theme.of(context).colorScheme.onSurface;
    final bgColor = iconBackgroundColor ?? AppColors.primary100;

    return Card(
      child: Padding(
        padding: AppSpacing.paddingCard,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(
                    color: bgColor,
                    borderRadius: AppSpacing.radiusSm,
                  ),
                  child: Icon(icon, size: 18, color: color),
                ),
                const SizedBox(width: AppSpacing.sm),
                Text(
                  label,
                  style: AppTypography.bodySmall.copyWith(
                    color: AppColors.neutral500,
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.sm + 4),
            Text(
              formatter.format(value),
              style: AppTypography.amountLarge.copyWith(color: color),
            ),
          ],
        ),
      ),
    );
  }
}
