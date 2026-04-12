import 'package:orizon/src/domain/entities/budget/budget_status_entity.dart';

class BudgetStatusModel extends BudgetStatusEntity {
  const BudgetStatusModel({
    required super.budgetId,
    required super.categoryId,
    required super.categoryName,
    required super.budgetAmount,
    required super.spentAmount,
    required super.percentageUsed,
  });

  factory BudgetStatusModel.fromJson(Map<String, dynamic> json) {
    return BudgetStatusModel(
      budgetId: json['budgetId'],
      categoryId: json['categoryId'],
      categoryName: json['categoryName'] ?? '',
      budgetAmount: (json['budgetAmount'] as num).toDouble(),
      spentAmount: (json['spentAmount'] as num).toDouble(),
      percentageUsed: (json['percentageUsed'] as num).toDouble(),
    );
  }
}
