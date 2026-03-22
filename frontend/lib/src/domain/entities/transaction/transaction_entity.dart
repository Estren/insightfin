import 'package:equatable/equatable.dart';

enum TransactionType { expense, income }

class TransactionEntity extends Equatable {
  final String id;
  final String categoryId;
  final TransactionType type;
  final double amount;
  final String? description;
  final DateTime date;
  final DateTime createdAt;

  const TransactionEntity({
    required this.id,
    required this.categoryId,
    required this.type,
    required this.amount,
    this.description,
    required this.date,
    required this.createdAt,
  });

  @override
  List<Object?> get props =>
      [id, categoryId, type, amount, description, date, createdAt];
}
