import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

class TransactionModel extends TransactionEntity {
  const TransactionModel({
    required super.id,
    required super.categoryId,
    required super.categoryName,
    required super.type,
    required super.amount,
    super.description,
    required super.date,
    required super.createdAt,
  });

  factory TransactionModel.fromJson(Map<String, dynamic> json) {
    return TransactionModel(
      id: json['id'],
      categoryId: json['categoryId'],
      categoryName: json['categoryName'] ?? '',
      type: json['type'] == 'EXPENSE'
          ? TransactionType.expense
          : TransactionType.income,
      amount: (json['amount'] as num).toDouble(),
      description: json['description'],
      date: DateTime.parse(json['date']),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'categoryId': categoryId,
      'type': type == TransactionType.expense ? 'EXPENSE' : 'INCOME',
      'amount': amount,
      'description': description,
      'date': '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}',
    };
  }
}
