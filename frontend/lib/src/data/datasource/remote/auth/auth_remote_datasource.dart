import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/src/data/model/auth/user_model.dart';

abstract class AuthRemoteDataSource {
  Future<UserModel> register(String name, String email, String password);
  Future<String> login(String email, String password);
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  final ApiClient apiClient;

  AuthRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<UserModel> register(String name, String email, String password) async {
    try {
      final response = await apiClient.dio.post('/auth/register', data: {
        'name': name,
        'email': email,
        'password': password,
      });
      return UserModel.fromJson(response.data);
    } catch (e) {
      throw const ServerException('Failed to register');
    }
  }

  @override
  Future<String> login(String email, String password) async {
    try {
      final response = await apiClient.dio.post('/auth/login', data: {
        'email': email,
        'password': password,
      });
      return response.data['token'];
    } catch (e) {
      throw const AuthException('Invalid email or password');
    }
  }
}
