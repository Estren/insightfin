import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/data/datasource/remote/transaction/transaction_remote_datasource.dart';
import 'package:orizon/src/data/model/transaction/transaction_model.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/domain/repository/transaction/transaction_repository.dart';

class TransactionRepositoryImpl implements TransactionRepository {
  final TransactionRemoteDataSource remoteDataSource;

  TransactionRepositoryImpl({required this.remoteDataSource});

  @override
  Future<Either<Failure, TransactionEntity>> create(
    String categoryId,
    TransactionType type,
    double amount,
    String? description,
    DateTime date,
  ) async {
    try {
      final model = TransactionModel(
        id: '',
        categoryId: categoryId,
        type: type,
        amount: amount,
        description: description,
        date: date,
        createdAt: DateTime.now(),
      );
      final result = await remoteDataSource.create(model.toJson());
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, List<TransactionEntity>>> getByDateRange(
    DateTime startDate,
    DateTime endDate,
  ) async {
    try {
      final start =
          '${startDate.year}-${startDate.month.toString().padLeft(2, '0')}-${startDate.day.toString().padLeft(2, '0')}';
      final end =
          '${endDate.year}-${endDate.month.toString().padLeft(2, '0')}-${endDate.day.toString().padLeft(2, '0')}';
      final result = await remoteDataSource.getByDateRange(start, end);
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }
}
