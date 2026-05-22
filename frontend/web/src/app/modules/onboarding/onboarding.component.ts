import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { NgClass } from '@angular/common';
import { ONBOARDING_FLAG } from '../../core/guards/onboarding.guard';
import { TransactionType } from '../../core/models/transaction.model';
import { BudgetStore } from '../../core/stores/budget.store';
import { CategoryStore } from '../../core/stores/category.store';
import { GoalStore } from '../../core/stores/goal.store';

@Component({
  selector: 'app-onboarding',
  templateUrl: './onboarding.component.html',
  imports: [NgClass, ReactiveFormsModule, TranslateModule],
})
export class OnboardingComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly categoryStore = inject(CategoryStore);
  private readonly budgetStore = inject(BudgetStore);
  private readonly goalStore = inject(GoalStore);

  readonly totalSteps = 3;
  step = 1;
  submitting = false;
  createdCategory: { id: string; name: string } | null = null;

  readonly categoryForm = this.fb.group({
    name: ['', Validators.required],
    type: ['EXPENSE' as TransactionType, Validators.required],
  });
  readonly budgetForm = this.fb.group({
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });
  readonly goalForm = this.fb.group({
    title: ['', Validators.required],
    targetAmount: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  selectType(type: TransactionType): void {
    this.categoryForm.controls.type.setValue(type);
  }

  submitCategory(): void {
    if (this.categoryForm.invalid || this.submitting) {
      return;
    }
    this.submitting = true;
    this.categoryStore.create({ name: this.categoryForm.value.name!, type: this.categoryForm.value.type! }).subscribe({
      next: (category) => {
        this.createdCategory = { id: category.id, name: category.name };
        this.submitting = false;
        this.step = 2;
      },
      error: () => {
        this.submitting = false;
      },
    });
  }

  submitBudget(): void {
    if (!this.createdCategory || this.budgetForm.invalid || this.submitting) {
      return;
    }
    this.submitting = true;
    this.budgetStore
      .create({
        categoryId: this.createdCategory.id,
        amount: this.budgetForm.value.amount!,
        month: OnboardingComponent.currentMonth(),
      })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.step = 3;
        },
        error: () => {
          this.submitting = false;
        },
      });
  }

  submitGoal(): void {
    if (this.goalForm.invalid || this.submitting) {
      return;
    }
    this.submitting = true;
    this.goalStore
      .create({ title: this.goalForm.value.title!, targetAmount: this.goalForm.value.targetAmount! })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.finish();
        },
        error: () => {
          this.submitting = false;
        },
      });
  }

  skip(): void {
    if (this.step < this.totalSteps) {
      this.step++;
    } else {
      this.finish();
    }
  }

  finish(): void {
    localStorage.setItem(ONBOARDING_FLAG, 'true');
    this.router.navigate(['/dashboard']);
  }

  private static currentMonth(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  }
}
