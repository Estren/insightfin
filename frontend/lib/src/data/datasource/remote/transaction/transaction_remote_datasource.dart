import 'package:orizon/core/error/exceptions.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/src/data/model/transaction/transaction_model.dart';

abstract class TransactionRemoteDataSource {
  Future<TransactionModel> create(Map<String, dynamic> data);
  Future<List<TransactionModel>> getByDateRange(
      String startDate, String endDate);
}

class TransactionRemoteDataSourceImpl implements TransactionRemoteDataSource {
  final ApiClient apiClient;

  TransactionRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<TransactionModel> create(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.dio.post('/transactions', data: data);
      return TransactionModel.fromJson(response.data);
    } catch (e) {
      throw const ServerException('Failed to create transaction');
    }
  }

  @override
  Future<List<TransactionModel>> getByDateRange(
      String startDate, String endDate) async {
    try {
      final response = await apiClient.dio.get('/transactions', queryParameters: {
        'startDate': startDate,
        'endDate': endDate,
      });
      return (response.data as List)
          .map((json) => TransactionModel.fromJson(json))
          .toList();
    } catch (e) {
      throw const ServerException('Failed to fetch transactions');
    }
  }
}
