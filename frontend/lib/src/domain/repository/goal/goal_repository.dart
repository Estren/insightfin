import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/domain/entities/goal/goal_contribution_entity.dart';

abstract class GoalRepository {
  Future<Either<Failure, GoalEntity>> create(
    String title,
    double targetAmount,
    DateTime? deadline,
  );
  Future<Either<Failure, GoalContributionEntity>> contribute(
    String goalId,
    double amount,
    DateTime date,
  );
}
