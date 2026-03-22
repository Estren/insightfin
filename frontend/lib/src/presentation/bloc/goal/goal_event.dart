import 'package:equatable/equatable.dart';

abstract class GoalEvent extends Equatable {
  const GoalEvent();

  @override
  List<Object?> get props => [];
}

class GoalsLoadRequested extends GoalEvent {}

class GoalCreateRequested extends GoalEvent {
  final String title;
  final double targetAmount;
  final DateTime? deadline;

  const GoalCreateRequested({
    required this.title,
    required this.targetAmount,
    this.deadline,
  });

  @override
  List<Object?> get props => [title, targetAmount, deadline];
}

class GoalContributeRequested extends GoalEvent {
  final String goalId;
  final double amount;
  final DateTime date;

  const GoalContributeRequested({
    required this.goalId,
    required this.amount,
    required this.date,
  });

  @override
  List<Object> get props => [goalId, amount, date];
}
