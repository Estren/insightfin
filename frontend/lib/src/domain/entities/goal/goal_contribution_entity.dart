import 'package:equatable/equatable.dart';

class GoalContributionEntity extends Equatable {
  final String id;
  final String goalId;
  final double amount;
  final DateTime date;
  final DateTime createdAt;

  const GoalContributionEntity({
    required this.id,
    required this.goalId,
    required this.amount,
    required this.date,
    required this.createdAt,
  });

  @override
  List<Object> get props => [id, goalId, amount, date, createdAt];
}
