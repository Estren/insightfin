import { Component, OnInit, input, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { TransactionType } from '../../../../core/models/transaction.model';
import { TransactionStore } from '../../../../core/stores/transaction.store';
import { TransactionFormComponent } from '../transaction-form/transaction-form.component';

@Component({
  selector: 'app-transaction-edit',
  templateUrl: './transaction-edit.component.html',
  imports: [ReactiveFormsModule, TransactionFormComponent, TranslateModule],
})
export class TransactionEditComponent implements OnInit {
  readonly transactionId = input.required<string>();
  readonly saved = output<void>();

  form!: FormGroup;
  submitting = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly transactionStore: TransactionStore,
  ) {}

  ngOnInit(): void {
    const transaction = this.transactionStore.findById(this.transactionId());
    if (!transaction) {
      this.saved.emit();
      return;
    }

    this.form = this.fb.group({
      type: [transaction.type as TransactionType, Validators.required],
      categoryId: [transaction.categoryId, Validators.required],
      amount: [transaction.amount, [Validators.required, Validators.min(0.01)]],
      description: [transaction.description ?? ''],
      date: [transaction.date, Validators.required],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;

    const { type, categoryId, amount, description, date } = this.form.value;
    this.transactionStore
      .update(this.transactionId(), {
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
}
