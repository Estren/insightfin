import 'package:go_router/go_router.dart';
import 'package:orizon/src/presentation/page/auth/login_page.dart';
import 'package:orizon/src/presentation/page/auth/register_page.dart';
import 'package:orizon/src/presentation/page/dashboard/dashboard_page.dart';

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
  ],
);
