import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:orizon/core/usecase/usecase.dart';
import 'package:orizon/src/domain/usecase/goal/contribute_to_goal_usecase.dart';
import 'package:orizon/src/domain/usecase/goal/create_goal_usecase.dart';
import 'package:orizon/src/domain/usecase/goal/get_goals_usecase.dart';
import 'goal_event.dart';
import 'goal_state.dart';

class GoalBloc extends Bloc<GoalEvent, GoalState> {
  final GetGoalsUseCase getGoalsUseCase;
  final CreateGoalUseCase createGoalUseCase;
  final ContributeToGoalUseCase contributeToGoalUseCase;

  GoalBloc({
    required this.getGoalsUseCase,
    required this.createGoalUseCase,
    required this.contributeToGoalUseCase,
  }) : super(GoalInitial()) {
    on<GoalsLoadRequested>(_onLoadRequested);
    on<GoalCreateRequested>(_onCreateRequested);
    on<GoalContributeRequested>(_onContributeRequested);
  }

  Future<void> _onLoadRequested(
      GoalsLoadRequested event, Emitter<GoalState> emit) async {
    emit(GoalLoading());
    final result = await getGoalsUseCase(const NoParams());
    result.fold(
      (failure) => emit(GoalError(failure.message)),
      (goals) => emit(GoalLoaded(goals)),
    );
  }

  Future<void> _onCreateRequested(
      GoalCreateRequested event, Emitter<GoalState> emit) async {
    emit(GoalLoading());
    final result = await createGoalUseCase(CreateGoalParams(
      title: event.title,
      targetAmount: event.targetAmount,
      deadline: event.deadline,
    ));
    result.fold(
      (failure) => emit(GoalError(failure.message)),
      (goal) => emit(GoalCreated(goal)),
    );
  }

  Future<void> _onContributeRequested(
      GoalContributeRequested event, Emitter<GoalState> emit) async {
    emit(GoalLoading());
    final result = await contributeToGoalUseCase(ContributeToGoalParams(
      goalId: event.goalId,
      amount: event.amount,
      date: event.date,
    ));
    result.fold(
      (failure) => emit(GoalError(failure.message)),
      (_) => emit(GoalContributed()),
    );
  }
}
