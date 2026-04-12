import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/domain/entities/budget/budget_entity.dart';
import 'package:orizon/src/domain/entities/budget/budget_status_entity.dart';

abstract class BudgetRepository {
  Future<Either<Failure, BudgetEntity>> create(
    String categoryId,
    double amount,
    String month,
  );
  Future<Either<Failure, List<BudgetEntity>>> getByMonth(String month);
  Future<Either<Failure, List<BudgetStatusEntity>>> getStatus(String month);
}
