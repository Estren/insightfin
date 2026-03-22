import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/src/data/model/goal/goal_model.dart';
import 'package:orizon/src/data/model/goal/goal_contribution_model.dart';

abstract class GoalRemoteDataSource {
  Future<List<GoalModel>> getAll();
  Future<GoalModel> create(Map<String, dynamic> data);
  Future<GoalContributionModel> contribute(
      String goalId, Map<String, dynamic> data);
}

class GoalRemoteDataSourceImpl implements GoalRemoteDataSource {
  final ApiClient apiClient;

  GoalRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<List<GoalModel>> getAll() async {
    try {
      final response = await apiClient.dio.get('/goals');
      return (response.data as List)
          .map((json) => GoalModel.fromJson(json))
          .toList();
    } catch (e) {
      throw const ServerException('Failed to fetch goals');
    }
  }

  @override
  Future<GoalModel> create(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.dio.post('/goals', data: data);
      return GoalModel.fromJson(response.data);
    } catch (e) {
      throw const ServerException('Failed to create goal');
    }
  }

  @override
  Future<GoalContributionModel> contribute(
      String goalId, Map<String, dynamic> data) async {
    try {
      final response =
          await apiClient.dio.post('/goals/$goalId/contributions', data: data);
      return GoalContributionModel.fromJson(response.data);
    } catch (e) {
      throw const ServerException('Failed to contribute to goal');
    }
  }
}
