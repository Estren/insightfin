import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/budget/budget_entity.dart';
import 'package:orizon/src/domain/repository/budget/budget_repository.dart';

class CreateBudgetUseCase extends UseCase<BudgetEntity, CreateBudgetParams> {
  final BudgetRepository repository;

  CreateBudgetUseCase(this.repository);

  @override
  Future<Either<Failure, BudgetEntity>> call(CreateBudgetParams params) {
    return repository.create(params.categoryId, params.amount, params.month);
  }
}

class CreateBudgetParams extends Equatable {
  final String categoryId;
  final double amount;
  final String month;

  const CreateBudgetParams({
    required this.categoryId,
    required this.amount,
    required this.month,
  });

  @override
  List<Object> get props => [categoryId, amount, month];
}
