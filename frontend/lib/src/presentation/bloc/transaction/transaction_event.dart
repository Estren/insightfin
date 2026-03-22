import 'package:equatable/equatable.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

abstract class TransactionEvent extends Equatable {
  const TransactionEvent();

  @override
  List<Object?> get props => [];
}

class TransactionsLoadRequested extends TransactionEvent {
  final DateTime startDate;
  final DateTime endDate;

  const TransactionsLoadRequested({
    required this.startDate,
    required this.endDate,
  });

  @override
  List<Object> get props => [startDate, endDate];
}

class TransactionCreateRequested extends TransactionEvent {
  final String categoryId;
  final TransactionType type;
  final double amount;
  final String? description;
  final DateTime date;

  const TransactionCreateRequested({
    required this.categoryId,
    required this.type,
    required this.amount,
    this.description,
    required this.date,
  });

  @override
  List<Object?> get props => [categoryId, type, amount, description, date];
}

class TransactionMonthChanged extends TransactionEvent {
  final DateTime month;

  const TransactionMonthChanged(this.month);

  @override
  List<Object> get props => [month];
}
