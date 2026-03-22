import 'package:equatable/equatable.dart';

class BudgetEntity extends Equatable {
  final String id;
  final String categoryId;
  final double amount;
  final String month;
  final DateTime createdAt;

  const BudgetEntity({
    required this.id,
    required this.categoryId,
    required this.amount,
    required this.month,
    required this.createdAt,
  });

  @override
  List<Object> get props => [id, categoryId, amount, month, createdAt];
}
