import 'package:equatable/equatable.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';

abstract class GoalState extends Equatable {
  const GoalState();

  @override
  List<Object?> get props => [];
}

class GoalInitial extends GoalState {}

class GoalLoading extends GoalState {}

class GoalLoaded extends GoalState {
  final List<GoalEntity> goals;

  const GoalLoaded(this.goals);

  List<GoalEntity> get activeGoals =>
      goals.where((g) => g.status == GoalStatus.active).toList();

  List<GoalEntity> get completedGoals =>
      goals.where((g) => g.status == GoalStatus.completed).toList();

  @override
  List<Object> get props => [goals];
}

class GoalCreated extends GoalState {
  final GoalEntity goal;

  const GoalCreated(this.goal);

  @override
  List<Object> get props => [goal];
}

class GoalContributed extends GoalState {}

class GoalError extends GoalState {
  final String message;

  const GoalError(this.message);

  @override
  List<Object> get props => [message];
}
