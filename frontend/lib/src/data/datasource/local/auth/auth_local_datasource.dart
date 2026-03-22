import 'package:orizon/config/constants.dart';
import 'package:orizon/core/error/exceptions.dart';
import 'package:shared_preferences/shared_preferences.dart';

abstract class AuthLocalDataSource {
  Future<void> saveToken(String token);
  Future<String?> getToken();
  Future<void> removeToken();
}

class AuthLocalDataSourceImpl implements AuthLocalDataSource {
  final SharedPreferences sharedPreferences;

  AuthLocalDataSourceImpl({required this.sharedPreferences});

  @override
  Future<void> saveToken(String token) async {
    final success =
        await sharedPreferences.setString(AppConstants.authTokenKey, token);
    if (!success) {
      throw const CacheException('Failed to save token');
    }
  }

  @override
  Future<String?> getToken() async {
    return sharedPreferences.getString(AppConstants.authTokenKey);
  }

  @override
  Future<void> removeToken() async {
    await sharedPreferences.remove(AppConstants.authTokenKey);
  }
}
