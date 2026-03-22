import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_bloc.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_event.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_state.dart';

class ContributeGoalPage extends StatefulWidget {
  final GoalEntity goal;

  const ContributeGoalPage({super.key, required this.goal});

  @override
  State<ContributeGoalPage> createState() => _ContributeGoalPageState();
}

class _ContributeGoalPageState extends State<ContributeGoalPage> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final formatter = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');
    final remaining = widget.goal.targetAmount - widget.goal.currentAmount;
    final percentage =
        (widget.goal.progressPercentage * 100).toStringAsFixed(0);

    return Scaffold(
      appBar: AppBar(title: Text(l10n.contribute)),
      body: BlocListener<GoalBloc, GoalState>(
        listener: (context, state) {
          if (state is GoalContributed) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(l10n.contribute)),
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
                // Goal summary card
                Card(
                  child: Padding(
                    padding: AppSpacing.paddingCard,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(widget.goal.title, style: AppTypography.h3),
                        const SizedBox(height: AppSpacing.sm),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              formatter.format(widget.goal.currentAmount),
                              style: AppTypography.amountSmall
                                  .copyWith(color: AppColors.primary600),
                            ),
                            Text(
                              formatter.format(widget.goal.targetAmount),
                              style: AppTypography.bodySmall
                                  .copyWith(color: AppColors.neutral500),
                            ),
                          ],
                        ),
                        const SizedBox(height: AppSpacing.xs),
                        ClipRRect(
                          borderRadius: AppSpacing.radiusFull,
                          child: LinearProgressIndicator(
                            value: widget.goal.progressPercentage,
                            backgroundColor: AppColors.neutral200,
                            color: AppColors.primary500,
                            minHeight: 8,
                          ),
                        ),
                        const SizedBox(height: AppSpacing.xs),
                        Text(
                          '$percentage% · ${formatter.format(remaining)} remaining',
                          style: AppTypography.bodySmall
                              .copyWith(color: AppColors.neutral500),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),

                // Amount input
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
                  autofocus: true,
                  validator: (value) {
                    if (value == null || value.isEmpty) return 'Required';
                    final parsed = double.tryParse(value);
                    if (parsed == null || parsed <= 0) return 'Invalid amount';
                    return null;
                  },
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
                            : Text(l10n.contribute),
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
    context.read<GoalBloc>().add(GoalContributeRequested(
          goalId: widget.goal.id,
          amount: double.parse(_amountController.text),
          date: DateTime.now(),
        ));
  }
}
