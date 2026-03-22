import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

class AmountText extends StatelessWidget {
  final double amount;
  final TransactionType? type;
  final TextStyle? style;
  final bool showSign;
  final String locale;

  const AmountText({
    super.key,
    required this.amount,
    this.type,
    this.style,
    this.showSign = true,
    this.locale = 'pt_BR',
  });

  const AmountText.income({
    super.key,
    required this.amount,
    this.style,
    this.showSign = true,
    this.locale = 'pt_BR',
  }) : type = TransactionType.income;

  const AmountText.expense({
    super.key,
    required this.amount,
    this.style,
    this.showSign = true,
    this.locale = 'pt_BR',
  }) : type = TransactionType.expense;

  @override
  Widget build(BuildContext context) {
    final formatter = NumberFormat.currency(locale: locale, symbol: 'R\$');
    final formatted = formatter.format(amount);

    final color = _resolveColor(context);
    final prefix = showSign ? _resolveSign() : '';

    return Text(
      '$prefix$formatted',
      style: (style ?? AppTypography.amount).copyWith(color: color),
    );
  }

  Color _resolveColor(BuildContext context) {
    if (type == TransactionType.income) return AppColors.income;
    if (type == TransactionType.expense) return AppColors.expense;
    return Theme.of(context).colorScheme.onSurface;
  }

  String _resolveSign() {
    if (type == TransactionType.income) return '+ ';
    if (type == TransactionType.expense) return '- ';
    return '';
  }
}
