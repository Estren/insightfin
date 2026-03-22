import 'package:equatable/equatable.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

abstract class CategoryEvent extends Equatable {
  const CategoryEvent();

  @override
  List<Object?> get props => [];
}

class CategoriesLoadRequested extends CategoryEvent {
  final TransactionType? type;

  const CategoriesLoadRequested({this.type});

  @override
  List<Object?> get props => [type];
}

class CategoryCreateRequested extends CategoryEvent {
  final String name;
  final TransactionType type;
  final String? icon;
  final String? color;

  const CategoryCreateRequested({
    required this.name,
    required this.type,
    this.icon,
    this.color,
  });

  @override
  List<Object?> get props => [name, type, icon, color];
}

class CategoryFilterChanged extends CategoryEvent {
  final TransactionType? type;

  const CategoryFilterChanged({this.type});

  @override
  List<Object?> get props => [type];
}
