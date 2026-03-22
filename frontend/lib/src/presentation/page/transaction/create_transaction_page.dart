import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/category/category_entity.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_state.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_bloc.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_event.dart';
import 'package:orizon/src/presentation/bloc/transaction/transaction_state.dart';

class CreateTransactionPage extends StatefulWidget {
  const CreateTransactionPage({super.key});

  @override
  State<CreateTransactionPage> createState() => _CreateTransactionPageState();
}

class _CreateTransactionPageState extends State<CreateTransactionPage> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _descriptionController = TextEditingController();
  TransactionType _selectedType = TransactionType.expense;
  CategoryEntity? _selectedCategory;
  DateTime _selectedDate = DateTime.now();

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.addTransaction)),
      body: BlocListener<TransactionBloc, TransactionState>(
        listener: (context, state) {
          if (state is TransactionCreated) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(l10n.addTransaction)),
            );
            context.pop();
          } else if (state is TransactionError) {
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
                // Type toggle
                Text(l10n.type, style: AppTypography.label),
                const SizedBox(height: AppSpacing.sm),
                _TypeToggle(
                  selected: _selectedType,
                  onChanged: (type) =>
                      setState(() {
                        _selectedType = type;
                        _selectedCategory = null;
                      }),
                ),
                const SizedBox(height: AppSpacing.lg),

                // Amount
                TextFormField(
                  controller: _amountController,
                  decoration: InputDecoration(
                    labelText: l10n.amount,
                    prefixIcon: const Icon(Icons.attach_money),
                    prefixText: 'R\$ ',
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
                const SizedBox(height: AppSpacing.md),

                // Category picker
                Text(l10n.categories, style: AppTypography.label),
                const SizedBox(height: AppSpacing.sm),
                BlocBuilder<CategoryBloc, CategoryState>(
                  builder: (context, state) {
                    if (state is CategoryLoaded) {
                      final filtered = state.categories
                          .where((c) => c.type == _selectedType)
                          .toList();

                      if (filtered.isEmpty) {
                        return Text(
                          l10n.noCategories,
                          style: AppTypography.body
                              .copyWith(color: AppColors.neutral500),
                        );
                      }

                      return _CategoryPicker(
                        categories: filtered,
                        selected: _selectedCategory,
                        onSelected: (cat) =>
                            setState(() => _selectedCategory = cat),
                      );
                    }
                    return const SizedBox.shrink();
                  },
                ),
                const SizedBox(height: AppSpacing.md),

                // Description
                TextFormField(
                  controller: _descriptionController,
                  decoration: InputDecoration(
                    labelText: l10n.description,
                    prefixIcon: const Icon(Icons.notes_outlined),
                  ),
                  maxLines: 1,
                ),
                const SizedBox(height: AppSpacing.md),

                // Date
                _DateField(
                  selectedDate: _selectedDate,
                  onChanged: (date) =>
                      setState(() => _selectedDate = date),
                ),
                const SizedBox(height: AppSpacing.xl),

                // Submit
                BlocBuilder<TransactionBloc, TransactionState>(
                  builder: (context, state) {
                    return SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: state is TransactionLoading
                            ? null
                            : _submit,
                        child: state is TransactionLoading
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

    context.read<TransactionBloc>().add(
          TransactionCreateRequested(
            categoryId: _selectedCategory!.id,
            type: _selectedType,
            amount: double.parse(_amountController.text),
            description: _descriptionController.text.trim().isEmpty
                ? null
                : _descriptionController.text.trim(),
            date: _selectedDate,
          ),
        );
  }
}

class _TypeToggle extends StatelessWidget {
  final TransactionType selected;
  final ValueChanged<TransactionType> onChanged;

  const _TypeToggle({required this.selected, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Row(
      children: [
        Expanded(
          child: _ToggleOption(
            label: l10n.expense,
            icon: Icons.arrow_downward,
            color: AppColors.expense,
            isSelected: selected == TransactionType.expense,
            onTap: () => onChanged(TransactionType.expense),
          ),
        ),
        const SizedBox(width: AppSpacing.sm),
        Expanded(
          child: _ToggleOption(
            label: l10n.income,
            icon: Icons.arrow_upward,
            color: AppColors.income,
            isSelected: selected == TransactionType.income,
            onTap: () => onChanged(TransactionType.income),
          ),
        ),
      ],
    );
  }
}

class _ToggleOption extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final bool isSelected;
  final VoidCallback onTap;

  const _ToggleOption({
    required this.label,
    required this.icon,
    required this.color,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: isSelected ? color.withOpacity(0.1) : Colors.transparent,
          border: Border.all(
            color: isSelected ? color : AppColors.neutral300,
            width: isSelected ? 2 : 1,
          ),
          borderRadius: AppSpacing.radiusSm,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon,
                color: isSelected ? color : AppColors.neutral500, size: 20),
            const SizedBox(width: AppSpacing.xs),
            Text(
              label,
              style: AppTypography.label.copyWith(
                color: isSelected ? color : AppColors.neutral500,
              ),
            ),
          ],
        ),
      ),
    );
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
    'salary': Icons.account_balance_outlined,
    'investment': Icons.trending_up_outlined,
    'gift': Icons.card_giftcard_outlined,
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
            padding: const EdgeInsets.symmetric(
                horizontal: 12, vertical: 8),
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

class _DateField extends StatelessWidget {
  final DateTime selectedDate;
  final ValueChanged<DateTime> onChanged;

  const _DateField({required this.selectedDate, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final formatted = DateFormat('dd/MM/yyyy').format(selectedDate);

    return GestureDetector(
      onTap: () async {
        final picked = await showDatePicker(
          context: context,
          initialDate: selectedDate,
          firstDate: DateTime(2020),
          lastDate: DateTime.now(),
        );
        if (picked != null) onChanged(picked);
      },
      child: InputDecorator(
        decoration: InputDecoration(
          labelText: l10n.date,
          prefixIcon: const Icon(Icons.calendar_today_outlined),
        ),
        child: Text(formatted, style: AppTypography.body),
      ),
    );
  }
}
