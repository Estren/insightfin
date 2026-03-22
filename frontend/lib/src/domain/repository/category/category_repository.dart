import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';

abstract class CategoryRepository {
  Future<Either<Failure, CategoryEntity>> create(
    String name,
    TransactionType type,
    String? icon,
    String? color,
  );
  Future<Either<Failure, List<CategoryEntity>>> getAll({TransactionType? type});
}
