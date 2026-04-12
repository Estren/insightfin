import 'package:equatable/equatable.dart';

class BudgetStatusEntity extends Equatable {
  final String budgetId;
  final String categoryId;
  final String categoryName;
  final double budgetAmount;
  final double spentAmount;
  final double percentageUsed;

  const BudgetStatusEntity({
    required this.budgetId,
    required this.categoryId,
    required this.categoryName,
    required this.budgetAmount,
    required this.spentAmount,
    required this.percentageUsed,
  });

  @override
  List<Object> get props =>
      [budgetId, categoryId, categoryName, budgetAmount, spentAmount, percentageUsed];
}
