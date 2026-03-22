import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:orizon/src/domain/usecase/category/create_category_usecase.dart';
import 'package:orizon/src/domain/usecase/category/get_categories_usecase.dart';
import 'category_event.dart';
import 'category_state.dart';

class CategoryBloc extends Bloc<CategoryEvent, CategoryState> {
  final GetCategoriesUseCase getCategoriesUseCase;
  final CreateCategoryUseCase createCategoryUseCase;

  CategoryBloc({
    required this.getCategoriesUseCase,
    required this.createCategoryUseCase,
  }) : super(CategoryInitial()) {
    on<CategoriesLoadRequested>(_onLoadRequested);
    on<CategoryCreateRequested>(_onCreateRequested);
    on<CategoryFilterChanged>(_onFilterChanged);
  }

  Future<void> _onLoadRequested(
      CategoriesLoadRequested event, Emitter<CategoryState> emit) async {
    emit(CategoryLoading());
    final result =
        await getCategoriesUseCase(GetCategoriesParams(type: event.type));
    result.fold(
      (failure) => emit(CategoryError(failure.message)),
      (categories) =>
          emit(CategoryLoaded(categories: categories, filterType: event.type)),
    );
  }

  Future<void> _onCreateRequested(
      CategoryCreateRequested event, Emitter<CategoryState> emit) async {
    emit(CategoryLoading());
    final result = await createCategoryUseCase(CreateCategoryParams(
      name: event.name,
      type: event.type,
      icon: event.icon,
      color: event.color,
    ));
    result.fold(
      (failure) => emit(CategoryError(failure.message)),
      (category) => emit(CategoryCreated(category)),
    );
  }

  Future<void> _onFilterChanged(
      CategoryFilterChanged event, Emitter<CategoryState> emit) async {
    emit(CategoryLoading());
    final result =
        await getCategoriesUseCase(GetCategoriesParams(type: event.type));
    result.fold(
      (failure) => emit(CategoryError(failure.message)),
      (categories) =>
          emit(CategoryLoaded(categories: categories, filterType: event.type)),
    );
  }
}
