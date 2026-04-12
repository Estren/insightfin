import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/data/datasource/remote/budget/budget_remote_datasource.dart';
import 'package:orizon/src/data/model/budget/budget_model.dart';
import 'package:orizon/src/domain/entities/budget/budget_entity.dart';
import 'package:orizon/src/domain/entities/budget/budget_status_entity.dart';
import 'package:orizon/src/domain/repository/budget/budget_repository.dart';

class BudgetRepositoryImpl implements BudgetRepository {
  final BudgetRemoteDataSource remoteDataSource;

  BudgetRepositoryImpl({required this.remoteDataSource});

  @override
  Future<Either<Failure, BudgetEntity>> create(
    String categoryId,
    double amount,
    String month,
  ) async {
    try {
      final model = BudgetModel(
        id: '',
        categoryId: categoryId,
        categoryName: '',
        amount: amount,
        month: month,
        createdAt: DateTime.now(),
      );
      final result = await remoteDataSource.create(model.toJson());
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, List<BudgetEntity>>> getByMonth(String month) async {
    try {
      final result = await remoteDataSource.getByMonth(month);
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, List<BudgetStatusEntity>>> getStatus(String month) async {
    try {
      final result = await remoteDataSource.getStatus(month);
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }
}
