import 'package:equatable/equatable.dart';

abstract class BudgetEvent extends Equatable {
  const BudgetEvent();

  @override
  List<Object?> get props => [];
}

class BudgetsLoadRequested extends BudgetEvent {
  final String month;

  const BudgetsLoadRequested({required this.month});

  @override
  List<Object> get props => [month];
}

class BudgetCreateRequested extends BudgetEvent {
  final String categoryId;
  final double amount;
  final String month;

  const BudgetCreateRequested({
    required this.categoryId,
    required this.amount,
    required this.month,
  });

  @override
  List<Object> get props => [categoryId, amount, month];
}

class BudgetMonthChanged extends BudgetEvent {
  final String month;

  const BudgetMonthChanged(this.month);

  @override
  List<Object> get props => [month];
}
