import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/data/datasource/local/auth/auth_local_datasource.dart';
import 'package:orizon/src/data/datasource/remote/auth/auth_remote_datasource.dart';
import 'package:orizon/src/domain/entities/auth/user_entity.dart';
import 'package:orizon/src/domain/repository/auth/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource remoteDataSource;
  final AuthLocalDataSource localDataSource;

  AuthRepositoryImpl({
    required this.remoteDataSource,
    required this.localDataSource,
  });

  @override
  Future<Either<Failure, UserEntity>> register(
      String name, String email, String password) async {
    try {
      final user = await remoteDataSource.register(name, email, password);
      return Right(user);
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, String>> login(String email, String password) async {
    try {
      final token = await remoteDataSource.login(email, password);
      await localDataSource.saveToken(token);
      return Right(token);
    } on AuthException catch (e) {
      return Left(AuthFailure(e.message));
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> logout() async {
    try {
      await localDataSource.removeToken();
      return const Right(null);
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, bool>> isAuthenticated() async {
    try {
      final token = await localDataSource.getToken();
      return Right(token != null);
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }
}
