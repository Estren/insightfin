import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/src/data/model/goal/goal_model.dart';
import 'package:orizon/src/data/model/goal/goal_contribution_model.dart';

abstract class GoalRemoteDataSource {
  Future<GoalModel> create(Map<String, dynamic> data);
  Future<GoalContributionModel> contribute(
      String goalId, Map<String, dynamic> data);
}

class GoalRemoteDataSourceImpl implements GoalRemoteDataSource {
  final ApiClient apiClient;

  GoalRemoteDataSourceImpl({required this.apiClient});

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
