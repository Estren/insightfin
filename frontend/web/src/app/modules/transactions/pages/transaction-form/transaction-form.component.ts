import { AsyncPipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, map, Observable, startWith } from 'rxjs';
import { CategoryResponse } from '../../../../core/models/category.model';
import { TransactionResponse, TransactionType } from '../../../../core/models/transaction.model';
import { CategoryStore } from '../../../../core/stores/category.store';
import { TransactionStore } from '../../../../core/stores/transaction.store';

@Component({
  selector: 'app-transaction-form',
  templateUrl: './transaction-form.component.html',
  imports: [AsyncPipe, NgClass, ReactiveFormsModule],
})
export class TransactionFormComponent implements OnInit {
  form!: FormGroup;
  editing: TransactionResponse | null = null;
  submitting = false;
  errorMessage = '';
  filteredCategories$!: Observable<CategoryResponse[]>;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly transactionStore: TransactionStore,
    private readonly categoryStore: CategoryStore,
  ) {}

  ngOnInit(): void {
    this.categoryStore.load();
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const transaction = this.transactionStore.findById(id);
      if (!transaction) {
        this.router.navigate(['/transactions']);
        return;
      }
      this.editing = transaction;
      this.form.patchValue({
        type: transaction.type,
        categoryId: transaction.categoryId,
        amount: transaction.amount,
        description: transaction.description ?? '',
        date: transaction.date,
      });
    }

    const typeControl = this.form.controls['type'];
    this.filteredCategories$ = combineLatest([
      this.categoryStore.categories$,
      typeControl.valueChanges.pipe(
        startWith(typeControl.value),
        map((v) => v as TransactionType),
      ),
    ]).pipe(map(([categories, type]) => categories.filter((c) => c.type === type)));
  }

  private initForm(): void {
    const today = this.todayIso();
    this.form = this.fb.group({
      type: ['EXPENSE' as TransactionType, Validators.required],
      categoryId: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: [''],
      date: [today, Validators.required],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;
    this.errorMessage = '';

    const { type, categoryId, amount, description, date } = this.form.value;
    const request = {
      type: type as TransactionType,
      categoryId: categoryId as string,
      amount: Number(amount),
      description: description ? String(description) : undefined,
      date: date as string,
    };

    const action$ = this.editing
      ? this.transactionStore.update(this.editing.id, request)
      : this.transactionStore.create(request);

    action$.subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/transactions']);
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'Failed to save transaction. Please try again.';
      },
    });
  }

  onCancel(): void {
    this.router.navigate(['/transactions']);
  }

  private todayIso(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
