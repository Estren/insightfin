import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/domain/repository/transaction/transaction_repository.dart';

class CreateTransactionUseCase
    extends UseCase<TransactionEntity, CreateTransactionParams> {
  final TransactionRepository repository;

  CreateTransactionUseCase(this.repository);

  @override
  Future<Either<Failure, TransactionEntity>> call(
      CreateTransactionParams params) {
    return repository.create(
      params.categoryId,
      params.type,
      params.amount,
      params.description,
      params.date,
    );
  }
}

class CreateTransactionParams extends Equatable {
  final String categoryId;
  final TransactionType type;
  final double amount;
  final String? description;
  final DateTime date;

  const CreateTransactionParams({
    required this.categoryId,
    required this.type,
    required this.amount,
    this.description,
    required this.date,
  });

  @override
  List<Object?> get props => [categoryId, type, amount, description, date];
}
