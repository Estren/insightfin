import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:orizon/config/injection_container.dart' as di;
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_event.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_bloc.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_event.dart';
import 'package:orizon/src/presentation/page/auth/login_page.dart';
import 'package:orizon/src/presentation/page/auth/register_page.dart';
import 'package:orizon/src/presentation/page/category/categories_page.dart';
import 'package:orizon/src/presentation/page/category/create_category_page.dart';
import 'package:orizon/src/presentation/page/dashboard/dashboard_page.dart';
import 'package:orizon/src/presentation/page/transaction/transactions_page.dart';
import 'package:orizon/src/presentation/page/transaction/create_transaction_page.dart';

final router = GoRouter(
  initialLocation: '/login',
  routes: [
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginPage(),
    ),
    GoRoute(
      path: '/register',
      builder: (context, state) => const RegisterPage(),
    ),
    GoRoute(
      path: '/dashboard',
      builder: (context, state) => const DashboardPage(),
    ),
    GoRoute(
      path: '/categories',
      builder: (context, state) => BlocProvider(
        create: (_) => di.sl<CategoryBloc>()
          ..add(const CategoriesLoadRequested()),
        child: const CategoriesPage(),
      ),
      routes: [
        GoRoute(
          path: 'create',
          builder: (context, state) => BlocProvider(
            create: (_) => di.sl<CategoryBloc>(),
            child: const CreateCategoryPage(),
          ),
        ),
      ],
    ),
    GoRoute(
      path: '/transactions',
      builder: (context, state) {
        final now = DateTime.now();
        return BlocProvider(
          create: (_) => di.sl<TransactionBloc>()
            ..add(TransactionsLoadRequested(
              startDate: DateTime(now.year, now.month, 1),
              endDate: DateTime(now.year, now.month + 1, 0),
            )),
          child: const TransactionsPage(),
        );
      },
      routes: [
        GoRoute(
          path: 'create',
          builder: (context, state) => MultiBlocProvider(
            providers: [
              BlocProvider(
                  create: (_) => di.sl<TransactionBloc>()),
              BlocProvider(
                  create: (_) => di.sl<CategoryBloc>()
                    ..add(const CategoriesLoadRequested())),
            ],
            child: const CreateTransactionPage(),
          ),
        ),
      ],
    ),
  ],
);
