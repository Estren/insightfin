import 'package:equatable/equatable.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

class CategoryEntity extends Equatable {
  final String id;
  final String name;
  final TransactionType type;
  final String? icon;
  final String? color;
  final DateTime createdAt;

  const CategoryEntity({
    required this.id,
    required this.name,
    required this.type,
    this.icon,
    this.color,
    required this.createdAt,
  });

  @override
  List<Object?> get props => [id, name, type, icon, color, createdAt];
}
