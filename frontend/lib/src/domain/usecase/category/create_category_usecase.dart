import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/domain/repository/category/category_repository.dart';

class CreateCategoryUseCase
    extends UseCase<CategoryEntity, CreateCategoryParams> {
  final CategoryRepository repository;

  CreateCategoryUseCase(this.repository);

  @override
  Future<Either<Failure, CategoryEntity>> call(CreateCategoryParams params) {
    return repository.create(params.name, params.type, params.icon, params.color);
  }
}

class CreateCategoryParams extends Equatable {
  final String name;
  final TransactionType type;
  final String? icon;
  final String? color;

  const CreateCategoryParams({
    required this.name,
    required this.type,
    this.icon,
    this.color,
  });

  @override
  List<Object?> get props => [name, type, icon, color];
}
