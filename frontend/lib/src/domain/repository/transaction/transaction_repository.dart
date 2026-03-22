import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

abstract class TransactionRepository {
  Future<Either<Failure, TransactionEntity>> create(
    String categoryId,
    TransactionType type,
    double amount,
    String? description,
    DateTime date,
  );
  Future<Either<Failure, List<TransactionEntity>>> getByDateRange(
    DateTime startDate,
    DateTime endDate,
  );
}
