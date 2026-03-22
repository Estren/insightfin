import 'package:equatable/equatable.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

abstract class CategoryState extends Equatable {
  const CategoryState();

  @override
  List<Object?> get props => [];
}

class CategoryInitial extends CategoryState {}

class CategoryLoading extends CategoryState {}

class CategoryLoaded extends CategoryState {
  final List<CategoryEntity> categories;
  final TransactionType? filterType;

  const CategoryLoaded({
    required this.categories,
    this.filterType,
  });

  @override
  List<Object?> get props => [categories, filterType];
}

class CategoryCreated extends CategoryState {
  final CategoryEntity category;

  const CategoryCreated(this.category);

  @override
  List<Object> get props => [category];
}

class CategoryError extends CategoryState {
  final String message;

  const CategoryError(this.message);

  @override
  List<Object> get props => [message];
}
