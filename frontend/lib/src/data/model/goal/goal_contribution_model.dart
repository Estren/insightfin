import 'package:orizon/src/domain/entities/goal/goal_contribution_entity.dart';

class GoalContributionModel extends GoalContributionEntity {
  const GoalContributionModel({
    required super.id,
    required super.goalId,
    required super.amount,
    required super.date,
    required super.createdAt,
  });

  factory GoalContributionModel.fromJson(Map<String, dynamic> json) {
    return GoalContributionModel(
      id: json['id'],
      goalId: json['goalId'],
      amount: (json['amount'] as num).toDouble(),
      date: DateTime.parse(json['date']),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'amount': amount,
      'date': '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}',
    };
  }
}
