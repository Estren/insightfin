import 'package:dio/dio.dart';
import 'package:orizon/core/network/token_interceptor.dart';

class ApiClient {
  final Dio dio;

  ApiClient({required this.dio, required TokenInterceptor tokenInterceptor}) {
    dio.options = BaseOptions(
      baseUrl: 'http://localhost:8080/api',
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {'Content-Type': 'application/json'},
    );
    dio.interceptors.add(tokenInterceptor);
  }
}
