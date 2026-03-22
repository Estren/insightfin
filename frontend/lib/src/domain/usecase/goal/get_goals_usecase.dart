import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/domain/repository/goal/goal_repository.dart';

class GetGoalsUseCase extends UseCase<List<GoalEntity>, NoParams> {
  final GoalRepository repository;

  GetGoalsUseCase(this.repository);

  @override
  Future<Either<Failure, List<GoalEntity>>> call(NoParams params) {
    return repository.getAll();
  }
}
