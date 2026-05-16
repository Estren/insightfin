import { AsyncPipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AiFeedbackResponse, AiFeedbackType } from '../../../../core/models/ai-feedback.model';
import { AiFeedbackStore } from '../../../../core/stores/ai-feedback.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { FeedbackCardComponent } from '../../../../shared/components/feedback-card/feedback-card.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

interface TypeChip {
  type: AiFeedbackType;
  labelKey: string;
}

@Component({
  selector: 'app-feedback-list',
  templateUrl: './feedback-list.component.html',
  imports: [
    AsyncPipe,
    NgClass,
    FormsModule,
    TranslateModule,
    CardComponent,
    EmptyStateComponent,
    FeedbackCardComponent,
    LoadingComponent,
    PageHeaderComponent,
  ],
})
export class FeedbackListComponent implements OnInit {
  readonly typeChips: TypeChip[] = [
    { type: 'MONTHLY_REPORT', labelKey: 'dashboard.feedbackTypes.monthlyReport' },
    { type: 'ALERT', labelKey: 'dashboard.feedbackTypes.alert' },
    { type: 'GOAL_PROJECTION', labelKey: 'dashboard.feedbackTypes.goalProjection' },
    { type: 'HEALTH_SCORE', labelKey: 'dashboard.feedbackTypes.healthScore' },
  ];

  selectedMonth = '';
  selectedTypes = new Set<AiFeedbackType>();

  constructor(public readonly feedbackStore: AiFeedbackStore) {}

  ngOnInit(): void {
    const now = new Date();
    this.selectedMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    this.feedbackStore.load(this.selectedMonth);
  }

  onMonthChange(value: string): void {
    if (!value) return;
    this.selectedMonth = value;
    this.feedbackStore.load(value);
  }

  toggleType(type: AiFeedbackType): void {
    if (this.selectedTypes.has(type)) {
      this.selectedTypes.delete(type);
    } else {
      this.selectedTypes.add(type);
    }
    this.selectedTypes = new Set(this.selectedTypes);
  }

  isSelected(type: AiFeedbackType): boolean {
    return this.selectedTypes.has(type);
  }

  applyTypeFilter(all: AiFeedbackResponse[]): AiFeedbackResponse[] {
    if (this.selectedTypes.size === 0) return all;
    return all.filter((f) => this.selectedTypes.has(f.type));
  }
}
