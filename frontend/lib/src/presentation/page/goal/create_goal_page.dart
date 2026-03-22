import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_bloc.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_event.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_state.dart';

class CreateGoalPage extends StatefulWidget {
  const CreateGoalPage({super.key});

  @override
  State<CreateGoalPage> createState() => _CreateGoalPageState();
}

class _CreateGoalPageState extends State<CreateGoalPage> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _amountController = TextEditingController();
  DateTime? _deadline;

  @override
  void dispose() {
    _titleController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.addGoal)),
      body: BlocListener<GoalBloc, GoalState>(
        listener: (context, state) {
          if (state is GoalCreated) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(l10n.addGoal)),
            );
            context.pop();
          } else if (state is GoalError) {
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
                  controller: _titleController,
                  decoration: InputDecoration(
                    labelText: l10n.name,
                    prefixIcon: const Icon(Icons.flag_outlined),
                  ),
                  validator: (value) =>
                      value == null || value.trim().isEmpty ? 'Required' : null,
                ),
                const SizedBox(height: AppSpacing.md),
                TextFormField(
                  controller: _amountController,
                  decoration: InputDecoration(
                    labelText: l10n.targetAmount,
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
                _DeadlineField(
                  deadline: _deadline,
                  onChanged: (date) => setState(() => _deadline = date),
                ),
                const SizedBox(height: AppSpacing.xl),
                BlocBuilder<GoalBloc, GoalState>(
                  builder: (context, state) {
                    return SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: state is GoalLoading ? null : _submit,
                        child: state is GoalLoading
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
    context.read<GoalBloc>().add(GoalCreateRequested(
          title: _titleController.text.trim(),
          targetAmount: double.parse(_amountController.text),
          deadline: _deadline,
        ));
  }
}

class _DeadlineField extends StatelessWidget {
  final DateTime? deadline;
  final ValueChanged<DateTime?> onChanged;

  const _DeadlineField({required this.deadline, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final formatted =
        deadline != null ? DateFormat('dd/MM/yyyy').format(deadline!) : '';

    return GestureDetector(
      onTap: () async {
        final picked = await showDatePicker(
          context: context,
          initialDate: deadline ?? DateTime.now().add(const Duration(days: 90)),
          firstDate: DateTime.now(),
          lastDate: DateTime.now().add(const Duration(days: 3650)),
        );
        if (picked != null) onChanged(picked);
      },
      child: InputDecorator(
        decoration: InputDecoration(
          labelText: '${l10n.deadline} (optional)',
          prefixIcon: const Icon(Icons.calendar_today_outlined),
          suffixIcon: deadline != null
              ? IconButton(
                  icon: const Icon(Icons.clear, size: 18),
                  onPressed: () => onChanged(null),
                )
              : null,
        ),
        child: Text(
          formatted.isEmpty ? '' : formatted,
          style: AppTypography.body,
        ),
      ),
    );
  }
}
