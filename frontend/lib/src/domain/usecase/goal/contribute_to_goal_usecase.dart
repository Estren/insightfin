import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/goal/goal_contribution_entity.dart';
import 'package:orizon/src/domain/repository/goal/goal_repository.dart';

class ContributeToGoalUseCase
    extends UseCase<GoalContributionEntity, ContributeToGoalParams> {
  final GoalRepository repository;

  ContributeToGoalUseCase(this.repository);

  @override
  Future<Either<Failure, GoalContributionEntity>> call(
      ContributeToGoalParams params) {
    return repository.contribute(params.goalId, params.amount, params.date);
  }
}

class ContributeToGoalParams extends Equatable {
  final String goalId;
  final double amount;
  final DateTime date;

  const ContributeToGoalParams({
    required this.goalId,
    required this.amount,
    required this.date,
  });

  @override
  List<Object> get props => [goalId, amount, date];
}
