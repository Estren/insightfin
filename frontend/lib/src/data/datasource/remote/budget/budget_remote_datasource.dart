import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/src/data/model/budget/budget_model.dart';

abstract class BudgetRemoteDataSource {
  Future<BudgetModel> create(Map<String, dynamic> data);
  Future<List<BudgetModel>> getByMonth(String month);
}

class BudgetRemoteDataSourceImpl implements BudgetRemoteDataSource {
  final ApiClient apiClient;

  BudgetRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<BudgetModel> create(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.dio.post('/budgets', data: data);
      return BudgetModel.fromJson(response.data);
    } catch (e) {
      throw const ServerException('Failed to create budget');
    }
  }

  @override
  Future<List<BudgetModel>> getByMonth(String month) async {
    try {
      final response = await apiClient.dio
          .get('/budgets', queryParameters: {'month': month});
      return (response.data as List)
          .map((json) => BudgetModel.fromJson(json))
          .toList();
    } catch (e) {
      throw const ServerException('Failed to fetch budgets');
    }
  }
}
