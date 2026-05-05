import { CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GoalResponse } from '../../../../core/models/goal.model';
import { GoalStore } from '../../../../core/stores/goal.store';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-goal-contribute',
  templateUrl: './goal-contribute.component.html',
  imports: [CurrencyPipe, ReactiveFormsModule, ModalComponent],
})
export class GoalContributeComponent implements OnInit {
  form!: FormGroup;
  goal: GoalResponse | null = null;
  submitting = false;
  errorMessage = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly goalStore: GoalStore,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/goals']);
      return;
    }
    const goal = this.goalStore.findById(id);
    if (!goal) {
      this.router.navigate(['/goals']);
      return;
    }
    this.goal = goal;

    this.form = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01)]],
      date: [this.today(), [Validators.required]],
    });
  }

  onSubmit(): void {
    if (!this.goal || this.form.invalid) return;
    this.submitting = true;
    this.errorMessage = '';

    const amount = Number(this.form.controls['amount'].value);
    const date = this.form.controls['date'].value as string;

    this.goalStore.contribute(this.goal.id, { amount, date }).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/goals']);
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'Failed to save contribution. Please try again.';
      },
    });
  }

  onCancel(): void {
    this.router.navigate(['/goals']);
  }

  private today(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
