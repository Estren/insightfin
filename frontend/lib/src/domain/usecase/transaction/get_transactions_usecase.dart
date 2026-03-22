import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/domain/repository/transaction/transaction_repository.dart';

class GetTransactionsUseCase
    extends UseCase<List<TransactionEntity>, GetTransactionsParams> {
  final TransactionRepository repository;

  GetTransactionsUseCase(this.repository);

  @override
  Future<Either<Failure, List<TransactionEntity>>> call(
      GetTransactionsParams params) {
    return repository.getByDateRange(params.startDate, params.endDate);
  }
}

class GetTransactionsParams extends Equatable {
  final DateTime startDate;
  final DateTime endDate;

  const GetTransactionsParams({
    required this.startDate,
    required this.endDate,
  });

  @override
  List<Object> get props => [startDate, endDate];
}
