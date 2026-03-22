import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:orizon/src/domain/usecase/transaction/create_transaction_usecase.dart';
import 'package:orizon/src/domain/usecase/transaction/get_transactions_usecase.dart';
import 'transaction_event.dart';
import 'transaction_state.dart';

class TransactionBloc extends Bloc<TransactionEvent, TransactionState> {
  final GetTransactionsUseCase getTransactionsUseCase;
  final CreateTransactionUseCase createTransactionUseCase;

  TransactionBloc({
    required this.getTransactionsUseCase,
    required this.createTransactionUseCase,
  }) : super(TransactionInitial()) {
    on<TransactionsLoadRequested>(_onLoadRequested);
    on<TransactionCreateRequested>(_onCreateRequested);
    on<TransactionMonthChanged>(_onMonthChanged);
  }

  Future<void> _onLoadRequested(
      TransactionsLoadRequested event, Emitter<TransactionState> emit) async {
    emit(TransactionLoading());
    final result = await getTransactionsUseCase(GetTransactionsParams(
      startDate: event.startDate,
      endDate: event.endDate,
    ));
    result.fold(
      (failure) => emit(TransactionError(failure.message)),
      (transactions) => emit(TransactionLoaded(
        transactions: transactions,
        selectedMonth: event.startDate,
      )),
    );
  }

  Future<void> _onCreateRequested(
      TransactionCreateRequested event, Emitter<TransactionState> emit) async {
    emit(TransactionLoading());
    final result = await createTransactionUseCase(CreateTransactionParams(
      categoryId: event.categoryId,
      type: event.type,
      amount: event.amount,
      description: event.description,
      date: event.date,
    ));
    result.fold(
      (failure) => emit(TransactionError(failure.message)),
      (transaction) => emit(TransactionCreated(transaction)),
    );
  }

  Future<void> _onMonthChanged(
      TransactionMonthChanged event, Emitter<TransactionState> emit) async {
    final start = DateTime(event.month.year, event.month.month, 1);
    final end = DateTime(event.month.year, event.month.month + 1, 0);
    emit(TransactionLoading());
    final result = await getTransactionsUseCase(GetTransactionsParams(
      startDate: start,
      endDate: end,
    ));
    result.fold(
      (failure) => emit(TransactionError(failure.message)),
      (transactions) => emit(TransactionLoaded(
        transactions: transactions,
        selectedMonth: start,
      )),
    );
  }
}
