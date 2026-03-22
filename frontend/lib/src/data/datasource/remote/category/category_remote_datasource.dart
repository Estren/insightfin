import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/src/data/model/category/category_model.dart';

abstract class CategoryRemoteDataSource {
  Future<CategoryModel> create(Map<String, dynamic> data);
  Future<List<CategoryModel>> getAll({String? type});
}

class CategoryRemoteDataSourceImpl implements CategoryRemoteDataSource {
  final ApiClient apiClient;

  CategoryRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<CategoryModel> create(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.dio.post('/categories', data: data);
      return CategoryModel.fromJson(response.data);
    } catch (e) {
      throw const ServerException('Failed to create category');
    }
  }

  @override
  Future<List<CategoryModel>> getAll({String? type}) async {
    try {
      final queryParams = <String, dynamic>{};
      if (type != null) queryParams['type'] = type;
      final response =
          await apiClient.dio.get('/categories', queryParameters: queryParams);
      return (response.data as List)
          .map((json) => CategoryModel.fromJson(json))
          .toList();
    } catch (e) {
      throw const ServerException('Failed to fetch categories');
    }
  }
}
