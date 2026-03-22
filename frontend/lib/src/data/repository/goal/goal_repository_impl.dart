import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/data/datasource/remote/goal/goal_remote_datasource.dart';
import 'package:orizon/src/data/model/goal/goal_model.dart';
import 'package:orizon/src/data/model/goal/goal_contribution_model.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/domain/entities/goal/goal_contribution_entity.dart';
import 'package:orizon/src/domain/repository/goal/goal_repository.dart';

class GoalRepositoryImpl implements GoalRepository {
  final GoalRemoteDataSource remoteDataSource;

  GoalRepositoryImpl({required this.remoteDataSource});

  @override
  Future<Either<Failure, GoalEntity>> create(
    String title,
    double targetAmount,
    DateTime? deadline,
  ) async {
    try {
      final model = GoalModel(
        id: '',
        title: title,
        targetAmount: targetAmount,
        currentAmount: 0,
        deadline: deadline,
        status: GoalStatus.active,
        createdAt: DateTime.now(),
      );
      final result = await remoteDataSource.create(model.toJson());
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, GoalContributionEntity>> contribute(
    String goalId,
    double amount,
    DateTime date,
  ) async {
    try {
      final model = GoalContributionModel(
        id: '',
        goalId: goalId,
        amount: amount,
        date: date,
        createdAt: DateTime.now(),
      );
      final result =
          await remoteDataSource.contribute(goalId, model.toJson());
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }
}
