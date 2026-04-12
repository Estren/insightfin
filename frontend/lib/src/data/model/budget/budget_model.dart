import 'package:orizon/src/domain/entities/budget/budget_entity.dart';

class BudgetModel extends BudgetEntity {
  const BudgetModel({
    required super.id,
    required super.categoryId,
    required super.categoryName,
    required super.amount,
    required super.month,
    required super.createdAt,
  });

  factory BudgetModel.fromJson(Map<String, dynamic> json) {
    return BudgetModel(
      id: json['id'],
      categoryId: json['categoryId'],
      categoryName: json['categoryName'] ?? '',
      amount: (json['amount'] as num).toDouble(),
      month: json['month'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'categoryId': categoryId,
      'amount': amount,
      'month': month,
    };
  }
}
