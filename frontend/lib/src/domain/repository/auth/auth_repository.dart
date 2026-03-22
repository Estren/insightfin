import 'package:dartz/dartz.dart';
import 'package:orizon/core/error/failures.dart';
import 'package:orizon/src/domain/entities/auth/user_entity.dart';

abstract class AuthRepository {
  Future<Either<Failure, UserEntity>> register(String name, String email, String password);
  Future<Either<Failure, String>> login(String email, String password);
  Future<Either<Failure, void>> logout();
  Future<Either<Failure, bool>> isAuthenticated();
}
