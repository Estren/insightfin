import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/src/domain/entities/goal/goal_entity.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_bloc.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_event.dart';
import 'package:orizon/src/presentation/bloc/goal/goal_state.dart';
import 'package:orizon/src/presentation/widget/empty_state.dart';
import 'package:orizon/src/presentation/widget/goal_card.dart';
import 'package:orizon/src/presentation/widget/loading_indicator.dart';

class GoalsPage extends StatelessWidget {
  const GoalsPage({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.goals)),
      body: BlocBuilder<GoalBloc, GoalState>(
        builder: (context, state) {
          if (state is GoalLoading) {
            return const LoadingIndicator();
          }

          if (state is GoalError) {
            return EmptyState(
              icon: Icons.error_outline,
              title: state.message,
            );
          }

          if (state is GoalLoaded) {
            if (state.goals.isEmpty) {
              return EmptyState(
                icon: Icons.flag_outlined,
                title: l10n.noGoals,
                actionLabel: l10n.addGoal,
                onAction: () => context.push('/goals/create'),
              );
            }

            return RefreshIndicator(
              onRefresh: () async {
                context.read<GoalBloc>().add(GoalsLoadRequested());
              },
              child: ListView(
                padding: const EdgeInsets.all(AppSpacing.md),
                children: [
                  if (state.activeGoals.isNotEmpty) ...[
                    _SectionHeader(
                      title: l10n.goals,
                      count: state.activeGoals.length,
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    ...state.activeGoals.map((goal) => Padding(
                          padding:
                              const EdgeInsets.only(bottom: AppSpacing.sm),
                          child: GoalCard(
                            goal: goal,
                            onTap: () => context.push(
                                  '/goals/${goal.id}/contribute',
                                  extra: goal,
                                ),
                          ),
                        )),
                  ],
                  if (state.completedGoals.isNotEmpty) ...[
                    const SizedBox(height: AppSpacing.md),
                    _SectionHeader(
                      title: 'Completed',
                      count: state.completedGoals.length,
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    ...state.completedGoals.map((goal) => Padding(
                          padding:
                              const EdgeInsets.only(bottom: AppSpacing.sm),
                          child: GoalCard(goal: goal),
                        )),
                  ],
                ],
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/goals/create'),
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  final int count;

  const _SectionHeader({required this.title, required this.count});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Text(title, style: AppTypography.h3),
        const SizedBox(width: AppSpacing.sm),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
          decoration: BoxDecoration(
            color: AppColors.neutral200,
            borderRadius: AppSpacing.radiusFull,
          ),
          child: Text(
            '$count',
            style: AppTypography.bodySmall.copyWith(
              color: AppColors.neutral600,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ],
    );
  }
}
