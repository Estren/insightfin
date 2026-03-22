import 'package:equatable/equatable.dart';

enum GoalStatus { active, completed, cancelled }

class GoalEntity extends Equatable {
  final String id;
  final String title;
  final double targetAmount;
  final double currentAmount;
  final DateTime? deadline;
  final GoalStatus status;
  final DateTime createdAt;

  const GoalEntity({
    required this.id,
    required this.title,
    required this.targetAmount,
    required this.currentAmount,
    this.deadline,
    required this.status,
    required this.createdAt,
  });

  double get progressPercentage =>
      targetAmount > 0 ? (currentAmount / targetAmount).clamp(0.0, 1.0) : 0.0;

  @override
  List<Object?> get props =>
      [id, title, targetAmount, currentAmount, deadline, status, createdAt];
}
