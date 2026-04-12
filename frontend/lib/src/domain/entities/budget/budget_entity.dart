import 'package:equatable/equatable.dart';

class BudgetEntity extends Equatable {
  final String id;
  final String categoryId;
  final String categoryName;
  final double amount;
  final String month;
  final DateTime createdAt;

  const BudgetEntity({
    required this.id,
    required this.categoryId,
    required this.categoryName,
    required this.amount,
    required this.month,
    required this.createdAt,
  });

  @override
  List<Object> get props => [id, categoryId, categoryName, amount, month, createdAt];
}
