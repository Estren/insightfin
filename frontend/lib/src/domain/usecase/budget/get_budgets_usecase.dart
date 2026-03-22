import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/budget/budget_entity.dart';
import 'package:orizon/src/domain/repository/budget/budget_repository.dart';

class GetBudgetsUseCase extends UseCase<List<BudgetEntity>, GetBudgetsParams> {
  final BudgetRepository repository;

  GetBudgetsUseCase(this.repository);

  @override
  Future<Either<Failure, List<BudgetEntity>>> call(GetBudgetsParams params) {
    return repository.getByMonth(params.month);
  }
}

class GetBudgetsParams extends Equatable {
  final String month;

  const GetBudgetsParams({required this.month});

  @override
  List<Object> get props => [month];
}
