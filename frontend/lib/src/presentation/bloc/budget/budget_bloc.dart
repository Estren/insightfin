import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:orizon/src/domain/usecase/budget/create_budget_usecase.dart';
import 'package:orizon/src/domain/usecase/budget/get_budget_status_usecase.dart';
import 'budget_event.dart';
import 'budget_state.dart';

class BudgetBloc extends Bloc<BudgetEvent, BudgetState> {
  final GetBudgetStatusUseCase getBudgetStatusUseCase;
  final CreateBudgetUseCase createBudgetUseCase;

  BudgetBloc({
    required this.getBudgetStatusUseCase,
    required this.createBudgetUseCase,
  }) : super(BudgetInitial()) {
    on<BudgetsLoadRequested>(_onLoadRequested);
    on<BudgetCreateRequested>(_onCreateRequested);
    on<BudgetMonthChanged>(_onMonthChanged);
  }

  Future<void> _onLoadRequested(
      BudgetsLoadRequested event, Emitter<BudgetState> emit) async {
    emit(BudgetLoading());
    final result = await getBudgetStatusUseCase(
        GetBudgetStatusParams(month: event.month));
    result.fold(
      (failure) => emit(BudgetError(failure.message)),
      (statuses) => emit(BudgetLoaded(
        statuses: statuses,
        selectedMonth: event.month,
      )),
    );
  }

  Future<void> _onCreateRequested(
      BudgetCreateRequested event, Emitter<BudgetState> emit) async {
    emit(BudgetLoading());
    final result = await createBudgetUseCase(CreateBudgetParams(
      categoryId: event.categoryId,
      amount: event.amount,
      month: event.month,
    ));
    result.fold(
      (failure) => emit(BudgetError(failure.message)),
      (budget) => emit(BudgetCreated(budget)),
    );
  }

  Future<void> _onMonthChanged(
      BudgetMonthChanged event, Emitter<BudgetState> emit) async {
    emit(BudgetLoading());
    final result = await getBudgetStatusUseCase(
        GetBudgetStatusParams(month: event.month));
    result.fold(
      (failure) => emit(BudgetError(failure.message)),
      (statuses) => emit(BudgetLoaded(
        statuses: statuses,
        selectedMonth: event.month,
      )),
    );
  }
}
