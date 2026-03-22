import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:orizon/core/network/api_client.dart';
import 'package:orizon/core/network/token_interceptor.dart';
import 'package:orizon/src/data/datasource/local/auth/auth_local_datasource.dart';
import 'package:orizon/src/data/datasource/remote/auth/auth_remote_datasource.dart';
import 'package:orizon/src/data/datasource/remote/transaction/transaction_remote_datasource.dart';
import 'package:orizon/src/data/datasource/remote/category/category_remote_datasource.dart';
import 'package:orizon/src/data/datasource/remote/goal/goal_remote_datasource.dart';
import 'package:orizon/src/data/datasource/remote/budget/budget_remote_datasource.dart';
import 'package:orizon/src/data/repository/auth/auth_repository_impl.dart';
import 'package:orizon/src/data/repository/transaction/transaction_repository_impl.dart';
import 'package:orizon/src/data/repository/category/category_repository_impl.dart';
import 'package:orizon/src/data/repository/goal/goal_repository_impl.dart';
import 'package:orizon/src/data/repository/budget/budget_repository_impl.dart';
import 'package:orizon/src/domain/repository/auth/auth_repository.dart';
import 'package:orizon/src/domain/repository/transaction/transaction_repository.dart';
import 'package:orizon/src/domain/repository/category/category_repository.dart';
import 'package:orizon/src/domain/repository/goal/goal_repository.dart';
import 'package:orizon/src/domain/repository/budget/budget_repository.dart';
import 'package:orizon/src/domain/usecase/auth/login_usecase.dart';
import 'package:orizon/src/domain/usecase/auth/register_usecase.dart';
import 'package:orizon/src/domain/usecase/transaction/create_transaction_usecase.dart';
import 'package:orizon/src/domain/usecase/transaction/get_transactions_usecase.dart';
import 'package:orizon/src/domain/usecase/category/create_category_usecase.dart';
import 'package:orizon/src/domain/usecase/category/get_categories_usecase.dart';
import 'package:orizon/src/domain/usecase/goal/create_goal_usecase.dart';
import 'package:orizon/src/domain/usecase/goal/contribute_to_goal_usecase.dart';
import 'package:orizon/src/domain/usecase/budget/create_budget_usecase.dart';
import 'package:orizon/src/domain/usecase/budget/get_budgets_usecase.dart';
import 'package:orizon/src/presentation/bloc/auth/auth_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';

final sl = GetIt.instance;

Future<void> init() async {
  // External
  final sharedPreferences = await SharedPreferences.getInstance();
  sl.registerLazySingleton(() => sharedPreferences);
  sl.registerLazySingleton(() => Dio());

  // Core
  sl.registerLazySingleton(() => TokenInterceptor(sharedPreferences: sl()));
  sl.registerLazySingleton(() => ApiClient(dio: sl(), tokenInterceptor: sl()));

  // Data Sources
  sl.registerLazySingleton<AuthLocalDataSource>(
      () => AuthLocalDataSourceImpl(sharedPreferences: sl()));
  sl.registerLazySingleton<AuthRemoteDataSource>(
      () => AuthRemoteDataSourceImpl(apiClient: sl()));
  sl.registerLazySingleton<TransactionRemoteDataSource>(
      () => TransactionRemoteDataSourceImpl(apiClient: sl()));
  sl.registerLazySingleton<CategoryRemoteDataSource>(
      () => CategoryRemoteDataSourceImpl(apiClient: sl()));
  sl.registerLazySingleton<GoalRemoteDataSource>(
      () => GoalRemoteDataSourceImpl(apiClient: sl()));
  sl.registerLazySingleton<BudgetRemoteDataSource>(
      () => BudgetRemoteDataSourceImpl(apiClient: sl()));

  // Repositories
  sl.registerLazySingleton<AuthRepository>(() => AuthRepositoryImpl(
      remoteDataSource: sl(), localDataSource: sl()));
  sl.registerLazySingleton<TransactionRepository>(
      () => TransactionRepositoryImpl(remoteDataSource: sl()));
  sl.registerLazySingleton<CategoryRepository>(
      () => CategoryRepositoryImpl(remoteDataSource: sl()));
  sl.registerLazySingleton<GoalRepository>(
      () => GoalRepositoryImpl(remoteDataSource: sl()));
  sl.registerLazySingleton<BudgetRepository>(
      () => BudgetRepositoryImpl(remoteDataSource: sl()));

  // Use Cases
  sl.registerLazySingleton(() => LoginUseCase(sl()));
  sl.registerLazySingleton(() => RegisterUseCase(sl()));
  sl.registerLazySingleton(() => CreateTransactionUseCase(sl()));
  sl.registerLazySingleton(() => GetTransactionsUseCase(sl()));
  sl.registerLazySingleton(() => CreateCategoryUseCase(sl()));
  sl.registerLazySingleton(() => GetCategoriesUseCase(sl()));
  sl.registerLazySingleton(() => CreateGoalUseCase(sl()));
  sl.registerLazySingleton(() => ContributeToGoalUseCase(sl()));
  sl.registerLazySingleton(() => CreateBudgetUseCase(sl()));
  sl.registerLazySingleton(() => GetBudgetsUseCase(sl()));

  // BLoCs
  sl.registerFactory(() => AuthBloc(
      loginUseCase: sl(), registerUseCase: sl(), authRepository: sl()));
  sl.registerFactory(() => CategoryBloc(
      getCategoriesUseCase: sl(), createCategoryUseCase: sl()));
}
