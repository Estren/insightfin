import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_bloc.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_event.dart';
import 'package:orizon/src/presentation/bloc/budget/budget_state.dart';
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_state.dart';

class CreateBudgetPage extends StatefulWidget {
  const CreateBudgetPage({super.key});

  @override
  State<CreateBudgetPage> createState() => _CreateBudgetPageState();
}

class _CreateBudgetPageState extends State<CreateBudgetPage> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  CategoryEntity? _selectedCategory;

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  String get _currentMonth {
    final now = DateTime.now();
    return '${now.year}-${now.month.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.addBudget)),
      body: BlocListener<BudgetBloc, BudgetState>(
        listener: (context, state) {
          if (state is BudgetCreated) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(l10n.addBudget)),
            );
            context.pop();
          } else if (state is BudgetError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(state.message)),
            );
          }
        },
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Category picker (expense only)
                Text(l10n.categories, style: AppTypography.label),
                const SizedBox(height: AppSpacing.sm),
                BlocBuilder<CategoryBloc, CategoryState>(
                  builder: (context, state) {
                    if (state is CategoryLoaded) {
                      final expenseCategories = state.categories
                          .where(
                              (c) => c.type == TransactionType.expense)
                          .toList();

                      if (expenseCategories.isEmpty) {
                        return Text(
                          l10n.noCategories,
                          style: AppTypography.body
                              .copyWith(color: AppColors.neutral500),
                        );
                      }

                      return _CategoryPicker(
                        categories: expenseCategories,
                        selected: _selectedCategory,
                        onSelected: (cat) =>
                            setState(() => _selectedCategory = cat),
                      );
                    }
                    return const SizedBox.shrink();
                  },
                ),
                const SizedBox(height: AppSpacing.lg),

                // Amount
                TextFormField(
                  controller: _amountController,
                  decoration: InputDecoration(
                    labelText: l10n.amount,
                    prefixIcon: const Icon(Icons.attach_money),
                    prefixText: 'R\$ ',
                    helperText: 'Monthly budget limit for this category',
                  ),
                  keyboardType:
                      const TextInputType.numberWithOptions(decimal: true),
                  inputFormatters: [
                    FilteringTextInputFormatter.allow(
                        RegExp(r'^\d+\.?\d{0,2}')),
                  ],
                  validator: (value) {
                    if (value == null || value.isEmpty) return 'Required';
                    final parsed = double.tryParse(value);
                    if (parsed == null || parsed <= 0) return 'Invalid amount';
                    return null;
                  },
                ),
                const SizedBox(height: AppSpacing.xl),

                // Submit
                BlocBuilder<BudgetBloc, BudgetState>(
                  builder: (context, state) {
                    return SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: state is BudgetLoading ? null : _submit,
                        child: state is BudgetLoading
                            ? const SizedBox(
                                height: 20,
                                width: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  color: Colors.white,
                                ),
                              )
                            : Text(l10n.save),
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedCategory == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a category')),
      );
      return;
    }

    context.read<BudgetBloc>().add(BudgetCreateRequested(
          categoryId: _selectedCategory!.id,
          amount: double.parse(_amountController.text),
          month: _currentMonth,
        ));
  }
}

class _CategoryPicker extends StatelessWidget {
  final List<CategoryEntity> categories;
  final CategoryEntity? selected;
  final ValueChanged<CategoryEntity> onSelected;

  const _CategoryPicker({
    required this.categories,
    required this.selected,
    required this.onSelected,
  });

  static const _iconMap = {
    'food': Icons.restaurant_outlined,
    'transport': Icons.directions_car_outlined,
    'shopping': Icons.shopping_bag_outlined,
    'health': Icons.favorite_outline,
    'education': Icons.school_outlined,
    'entertainment': Icons.movie_outlined,
    'housing': Icons.home_outlined,
    'other': Icons.more_horiz_outlined,
  };

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: AppSpacing.sm,
      runSpacing: AppSpacing.sm,
      children: categories.map((cat) {
        final isSelected = selected?.id == cat.id;
        final icon = _iconMap[cat.icon] ?? Icons.category_outlined;

        return GestureDetector(
          onTap: () => onSelected(cat),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            padding:
                const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: isSelected
                  ? AppColors.primary100
                  : AppColors.neutral100,
              border: Border.all(
                color: isSelected
                    ? AppColors.primary600
                    : Colors.transparent,
                width: 2,
              ),
              borderRadius: AppSpacing.radiusFull,
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  icon,
                  size: 18,
                  color: isSelected
                      ? AppColors.primary700
                      : AppColors.neutral500,
                ),
                const SizedBox(width: 6),
                Text(
                  cat.name,
                  style: AppTypography.bodySmall.copyWith(
                    color: isSelected
                        ? AppColors.primary700
                        : AppColors.neutral600,
                    fontWeight:
                        isSelected ? FontWeight.w600 : FontWeight.w400,
                  ),
                ),
              ],
            ),
          ),
        );
      }).toList(),
    );
  }
}
