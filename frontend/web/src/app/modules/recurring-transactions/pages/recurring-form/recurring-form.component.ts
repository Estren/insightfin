import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { combineLatest, map, Observable } from 'rxjs';
import { CategoryResponse } from '../../../../core/models/category.model';
import {
  CreateRecurringTransactionRequest,
  RecurrenceFrequency,
  RecurringTransactionResponse,
} from '../../../../core/models/recurring-transaction.model';
import { TransactionType } from '../../../../core/models/transaction.model';
import { CategoryStore } from '../../../../core/stores/category.store';
import { RecurringTransactionStore } from '../../../../core/stores/recurring-transaction.store';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-recurring-form',
  templateUrl: './recurring-form.component.html',
  imports: [AsyncPipe, ReactiveFormsModule, ModalComponent, TranslateModule],
})
export class RecurringFormComponent implements OnInit {
  form!: FormGroup;
  editing: RecurringTransactionResponse | null = null;
  submitting = false;
  categories$!: Observable<CategoryResponse[]>;

  readonly frequencies: RecurrenceFrequency[] = ['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'];

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly store: RecurringTransactionStore,
    private readonly categoryStore: CategoryStore,
  ) {}

  ngOnInit(): void {
    this.categoryStore.load();
    this.initForm();

    this.categories$ = combineLatest([
      this.categoryStore.categories$,
      this.form.controls['type'].valueChanges.pipe(map((v) => v as TransactionType)),
    ]).pipe(map(([list, type]) => list.filter((c) => c.type === type)));

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.store.items$.subscribe((items) => {
        const found = items.find((r) => r.id === id);
        if (!found) return;
        this.editing = found;
        this.form.patchValue({
          type: found.type,
          categoryId: found.categoryId,
          amount: found.amount,
          description: found.description || '',
          frequency: found.frequency,
          startDate: found.startDate,
          endDate: found.endDate || '',
        });
      });
    }
  }

  private initForm(): void {
    const today = new Date().toISOString().slice(0, 10);
    this.form = this.fb.group({
      type: ['EXPENSE' as TransactionType, Validators.required],
      categoryId: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: [''],
      frequency: ['MONTHLY' as RecurrenceFrequency, Validators.required],
      startDate: [today, Validators.required],
      endDate: [''],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;

    const v = this.form.value;
    const request: CreateRecurringTransactionRequest = {
      categoryId: v.categoryId,
      type: v.type,
      amount: Number(v.amount),
      description: v.description || undefined,
      frequency: v.frequency,
      startDate: v.startDate,
      endDate: v.endDate || undefined,
    };

    const obs = this.editing
      ? this.store.update(this.editing.id, request)
      : this.store.create(request);

    obs.subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/recurring']);
      },
      error: () => {
        this.submitting = false;
      },
    });
  }

  onCancel(): void {
    this.router.navigate(['/recurring']);
  }
}
