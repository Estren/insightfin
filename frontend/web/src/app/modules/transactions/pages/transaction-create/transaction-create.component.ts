import { Component, OnInit, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { TransactionType } from '../../../../core/models/transaction.model';
import { TransactionStore } from '../../../../core/stores/transaction.store';
import { TransactionFormComponent } from '../transaction-form/transaction-form.component';

@Component({
  selector: 'app-transaction-create',
  templateUrl: './transaction-create.component.html',
  imports: [ReactiveFormsModule, TransactionFormComponent, TranslateModule],
})
export class TransactionCreateComponent implements OnInit {
  readonly saved = output<void>();

  form!: FormGroup;
  submitting = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly transactionStore: TransactionStore,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      type: ['EXPENSE' as TransactionType, Validators.required],
      categoryId: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: [''],
      date: [this.todayIso(), Validators.required],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;

    const { type, categoryId, amount, description, date } = this.form.value;
    this.transactionStore
      .create({
        type: type as TransactionType,
        categoryId: categoryId as string,
        amount: Number(amount),
        description: description ? String(description) : undefined,
        date: date as string,
      })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.saved.emit();
        },
        error: () => {
          this.submitting = false;
        },
      });
  }

  private todayIso(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
