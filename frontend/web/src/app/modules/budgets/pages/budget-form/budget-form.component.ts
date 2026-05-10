import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { map, Observable } from 'rxjs';
import { BudgetResponse } from '../../../../core/models/budget.model';
import { CategoryResponse } from '../../../../core/models/category.model';
import { BudgetStore } from '../../../../core/stores/budget.store';
import { CategoryStore } from '../../../../core/stores/category.store';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-budget-form',
  templateUrl: './budget-form.component.html',
  imports: [AsyncPipe, ReactiveFormsModule, ModalComponent, TranslateModule],
})
export class BudgetFormComponent implements OnInit {
  form!: FormGroup;
  editing: BudgetResponse | null = null;
  submitting = false;
  expenseCategories$!: Observable<CategoryResponse[]>;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly budgetStore: BudgetStore,
    private readonly categoryStore: CategoryStore,
  ) {}

  ngOnInit(): void {
    this.categoryStore.load('EXPENSE');
    this.expenseCategories$ = this.categoryStore.categories$.pipe(
      map((list) => list.filter((c) => c.type === 'EXPENSE')),
    );
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const budget = this.budgetStore.findById(id);
      if (!budget) {
        this.router.navigate(['/budgets']);
        return;
      }
      this.editing = budget;
      this.form.patchValue({
        categoryId: budget.categoryId,
        amount: budget.amount,
        month: budget.month,
      });
      this.form.controls['categoryId'].disable();
      this.form.controls['month'].disable();
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      categoryId: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      month: [this.budgetStore.selectedMonth, [Validators.required, Validators.pattern(/^\d{4}-\d{2}$/)]],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;

    const amount = Number(this.form.controls['amount'].value);

    if (this.editing) {
      this.budgetStore.update(this.editing.id, { amount }).subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/budgets']);
        },
        error: () => {
          this.submitting = false;
        },
      });
      return;
    }

    const categoryId = this.form.controls['categoryId'].value as string;
    const month = this.form.controls['month'].value as string;
    this.budgetStore.create({ categoryId, amount, month }).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/budgets']);
      },
      error: () => {
        this.submitting = false;
      },
    });
  }

  onCancel(): void {
    this.router.navigate(['/budgets']);
  }
}
