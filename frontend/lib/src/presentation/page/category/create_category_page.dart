import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/transaction/transaction_entity.dart';
import 'package:orizon/src/presentation/bloc/category/category_bloc.dart';
import 'package:orizon/src/presentation/bloc/category/category_event.dart';
import 'package:orizon/src/presentation/bloc/category/category_state.dart';

class CreateCategoryPage extends StatefulWidget {
  const CreateCategoryPage({super.key});

  @override
  State<CreateCategoryPage> createState() => _CreateCategoryPageState();
}

class _CreateCategoryPageState extends State<CreateCategoryPage> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  TransactionType _selectedType = TransactionType.expense;
  String _selectedIcon = 'other';

  static const _iconOptions = {
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
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.createCategory)),
      body: BlocListener<CategoryBloc, CategoryState>(
        listener: (context, state) {
          if (state is CategoryCreated) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(l10n.categoryCreated)),
            );
            context.pop();
          } else if (state is CategoryError) {
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
                TextFormField(
                  controller: _nameController,
                  decoration: InputDecoration(
                    labelText: l10n.categoryName,
                    prefixIcon: const Icon(Icons.label_outlined),
                  ),
                  validator: (value) =>
                      value == null || value.trim().isEmpty ? 'Required' : null,
                ),
                const SizedBox(height: AppSpacing.lg),
                Text(l10n.type, style: AppTypography.label),
                const SizedBox(height: AppSpacing.sm),
                _TypeSelector(
                  selected: _selectedType,
                  onChanged: (type) => setState(() => _selectedType = type),
                ),
                const SizedBox(height: AppSpacing.lg),
                Text('Icon', style: AppTypography.label),
                const SizedBox(height: AppSpacing.sm),
                _IconGrid(
                  icons: _iconOptions,
                  selected: _selectedIcon,
                  onSelected: (icon) => setState(() => _selectedIcon = icon),
                ),
                const SizedBox(height: AppSpacing.xl),
                BlocBuilder<CategoryBloc, CategoryState>(
                  builder: (context, state) {
                    return SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: state is CategoryLoading
                            ? null
                            : () {
                                if (_formKey.currentState!.validate()) {
                                  context.read<CategoryBloc>().add(
                                        CategoryCreateRequested(
                                          name: _nameController.text.trim(),
                                          type: _selectedType,
                                          icon: _selectedIcon,
                                        ),
                                      );
                                }
                              },
                        child: state is CategoryLoading
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
}

class _TypeSelector extends StatelessWidget {
  final TransactionType selected;
  final ValueChanged<TransactionType> onChanged;

  const _TypeSelector({required this.selected, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Row(
      children: [
        Expanded(
          child: _TypeOption(
            label: l10n.expense,
            icon: Icons.arrow_downward,
            color: AppColors.expense,
            isSelected: selected == TransactionType.expense,
            onTap: () => onChanged(TransactionType.expense),
          ),
        ),
        const SizedBox(width: AppSpacing.sm),
        Expanded(
          child: _TypeOption(
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

class _TypeOption extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final bool isSelected;
  final VoidCallback onTap;

  const _TypeOption({
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
            Icon(icon, color: isSelected ? color : AppColors.neutral500, size: 20),
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

class _IconGrid extends StatelessWidget {
  final Map<String, IconData> icons;
  final String selected;
  final ValueChanged<String> onSelected;

  const _IconGrid({
    required this.icons,
    required this.selected,
    required this.onSelected,
  });

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: AppSpacing.sm,
      runSpacing: AppSpacing.sm,
      children: icons.entries.map((entry) {
        final isSelected = entry.key == selected;
        return GestureDetector(
          onTap: () => onSelected(entry.key),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            width: 52,
            height: 52,
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
              borderRadius: AppSpacing.radiusSm,
            ),
            child: Icon(
              entry.value,
              color: isSelected
                  ? AppColors.primary700
                  : AppColors.neutral500,
              size: 24,
            ),
          ),
        );
      }).toList(),
    );
  }
}
