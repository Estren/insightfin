import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:orizon/config/injection_container.dart' as di;
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_event.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_bloc.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_event.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_bloc.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_event.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_bloc.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_event.dart';
import 'package:orizon/src/presentation/page/auth/login_page.dart';
import 'package:orizon/src/presentation/page/auth/register_page.dart';
import 'package:orizon/src/presentation/page/category/categories_page.dart';
import 'package:orizon/src/presentation/page/category/create_category_page.dart';
import 'package:orizon/src/presentation/page/dashboard/dashboard_page.dart';
import 'package:orizon/src/presentation/page/goal/goals_page.dart';
import 'package:orizon/src/presentation/page/goal/create_goal_page.dart';
import 'package:orizon/src/presentation/page/goal/contribute_goal_page.dart';
import 'package:orizon/src/presentation/page/budget/budgets_page.dart';
import 'package:orizon/src/presentation/page/budget/create_budget_page.dart';
import 'package:orizon/src/presentation/page/splash/splash_page.dart';
import 'package:orizon/src/presentation/page/transaction/transactions_page.dart';
import 'package:orizon/src/presentation/page/transaction/create_transaction_page.dart';

final router = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => const SplashPage(),
    ),
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
      builder: (context, state) {
        final now = DateTime.now();
        final month =
            '${now.year}-${now.month.toString().padLeft(2, '0')}';
        return MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (_) => di.sl<TransactionBloc>()
                ..add(TransactionsLoadRequested(
                  startDate: DateTime(now.year, now.month, 1),
                  endDate: DateTime(now.year, now.month + 1, 0),
                )),
            ),
            BlocProvider(
              create: (_) =>
                  di.sl<GoalBloc>()..add(GoalsLoadRequested()),
            ),
            BlocProvider(
              create: (_) => di.sl<BudgetBloc>()
                ..add(BudgetsLoadRequested(month: month)),
            ),
          ],
          child: const DashboardPage(),
        );
      },
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
    GoRoute(
      path: '/goals',
      builder: (context, state) => BlocProvider(
        create: (_) => di.sl<GoalBloc>()..add(GoalsLoadRequested()),
        child: const GoalsPage(),
      ),
      routes: [
        GoRoute(
          path: 'create',
          builder: (context, state) => BlocProvider(
            create: (_) => di.sl<GoalBloc>(),
            child: const CreateGoalPage(),
          ),
        ),
        GoRoute(
          path: ':goalId/contribute',
          builder: (context, state) {
            final goal = state.extra as GoalEntity;
            return BlocProvider(
              create: (_) => di.sl<GoalBloc>(),
              child: ContributeGoalPage(goal: goal),
            );
          },
        ),
      ],
    ),
    GoRoute(
      path: '/budgets',
      builder: (context, state) {
        final now = DateTime.now();
        final month =
            '${now.year}-${now.month.toString().padLeft(2, '0')}';
        return BlocProvider(
          create: (_) => di.sl<BudgetBloc>()
            ..add(BudgetsLoadRequested(month: month)),
          child: const BudgetsPage(),
        );
      },
      routes: [
        GoRoute(
          path: 'create',
          builder: (context, state) => MultiBlocProvider(
            providers: [
              BlocProvider(create: (_) => di.sl<BudgetBloc>()),
              BlocProvider(
                  create: (_) => di.sl<CategoryBloc>()
                    ..add(const CategoriesLoadRequested())),
            ],
            child: const CreateBudgetPage(),
          ),
        ),
      ],
    ),
  ],
);
