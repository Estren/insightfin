import 'package:equatable/equatable.dart';
import 'package:orizon/src/domain/entities/budget/budget_entity.dart';
import 'package:orizon/src/domain/entities/budget/budget_status_entity.dart';

abstract class BudgetState extends Equatable {
  const BudgetState();

  @override
  List<Object?> get props => [];
}

class BudgetInitial extends BudgetState {}

class BudgetLoading extends BudgetState {}

class BudgetLoaded extends BudgetState {
  final List<BudgetStatusEntity> statuses;
  final String selectedMonth;

  const BudgetLoaded({
    required this.statuses,
    required this.selectedMonth,
  });

  double get totalBudgeted =>
      statuses.fold(0.0, (sum, s) => sum + s.budgetAmount);

  @override
  List<Object> get props => [statuses, selectedMonth];
}

class BudgetCreated extends BudgetState {
  final BudgetEntity budget;

  const BudgetCreated(this.budget);

  @override
  List<Object> get props => [budget];
}

class BudgetError extends BudgetState {
  final String message;

  const BudgetError(this.message);

  @override
  List<Object> get props => [message];
}
