import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

class CategoryModel extends CategoryEntity {
  const CategoryModel({
    required super.id,
    required super.name,
    required super.type,
    super.icon,
    super.color,
    required super.createdAt,
  });

  factory CategoryModel.fromJson(Map<String, dynamic> json) {
    return CategoryModel(
      id: json['id'],
      name: json['name'],
      type: json['type'] == 'EXPENSE'
          ? TransactionType.expense
          : TransactionType.income,
      icon: json['icon'],
      color: json['color'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'type': type == TransactionType.expense ? 'EXPENSE' : 'INCOME',
      'icon': icon,
      'color': color,
    };
  }
}
