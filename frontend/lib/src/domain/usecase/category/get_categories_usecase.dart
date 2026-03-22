import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/domain/repository/category/category_repository.dart';

class GetCategoriesUseCase
    extends UseCase<List<CategoryEntity>, GetCategoriesParams> {
  final CategoryRepository repository;

  GetCategoriesUseCase(this.repository);

  @override
  Future<Either<Failure, List<CategoryEntity>>> call(
      GetCategoriesParams params) {
    return repository.getAll(type: params.type);
  }
}

class GetCategoriesParams {
  final TransactionType? type;

  const GetCategoriesParams({this.type});
}
