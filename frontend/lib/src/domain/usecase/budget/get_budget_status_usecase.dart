import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/budget/budget_status_entity.dart';
import 'package:orizon/src/domain/repository/budget/budget_repository.dart';

class GetBudgetStatusUseCase extends UseCase<List<BudgetStatusEntity>, GetBudgetStatusParams> {
  final BudgetRepository repository;

  GetBudgetStatusUseCase(this.repository);

  @override
  Future<Either<Failure, List<BudgetStatusEntity>>> call(GetBudgetStatusParams params) {
    return repository.getStatus(params.month);
  }
}

class GetBudgetStatusParams extends Equatable {
  final String month;

  const GetBudgetStatusParams({required this.month});

  @override
  List<Object> get props => [month];
}
