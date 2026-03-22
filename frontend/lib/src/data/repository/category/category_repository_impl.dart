import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/data/datasource/remote/category/category_remote_datasource.dart';
import 'package:orizon/src/data/model/category/category_model.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/domain/repository/category/category_repository.dart';

class CategoryRepositoryImpl implements CategoryRepository {
  final CategoryRemoteDataSource remoteDataSource;

  CategoryRepositoryImpl({required this.remoteDataSource});

  @override
  Future<Either<Failure, CategoryEntity>> create(
    String name,
    TransactionType type,
    String? icon,
    String? color,
  ) async {
    try {
      final model = CategoryModel(
        id: '',
        name: name,
        type: type,
        icon: icon,
        color: color,
        createdAt: DateTime.now(),
      );
      final result = await remoteDataSource.create(model.toJson());
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, List<CategoryEntity>>> getAll(
      {TransactionType? type}) async {
    try {
      final typeStr = type != null
          ? (type == TransactionType.expense ? 'EXPENSE' : 'INCOME')
          : null;
      final result = await remoteDataSource.getAll(type: typeStr);
      return Right(result);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }
}
