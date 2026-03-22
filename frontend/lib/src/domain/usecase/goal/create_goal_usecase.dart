import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/domain/repository/goal/goal_repository.dart';

class CreateGoalUseCase extends UseCase<GoalEntity, CreateGoalParams> {
  final GoalRepository repository;

  CreateGoalUseCase(this.repository);

  @override
  Future<Either<Failure, GoalEntity>> call(CreateGoalParams params) {
    return repository.create(params.title, params.targetAmount, params.deadline);
  }
}

class CreateGoalParams extends Equatable {
  final String title;
  final double targetAmount;
  final DateTime? deadline;

  const CreateGoalParams({
    required this.title,
    required this.targetAmount,
    this.deadline,
  });

  @override
  List<Object?> get props => [title, targetAmount, deadline];
}
