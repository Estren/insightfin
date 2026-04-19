import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GoalResponse } from '../../../../core/models/goal.model';
import { GoalStore } from '../../../../core/stores/goal.store';

@Component({
  selector: 'app-goal-form',
  templateUrl: './goal-form.component.html',
  imports: [ReactiveFormsModule],
})
export class GoalFormComponent implements OnInit {
  form!: FormGroup;
  editing: GoalResponse | null = null;
  submitting = false;
  errorMessage = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly goalStore: GoalStore,
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const goal = this.goalStore.findById(id);
      if (!goal) {
        this.router.navigate(['/goals']);
        return;
      }
      this.editing = goal;
      this.form.patchValue({
        title: goal.title,
        targetAmount: goal.targetAmount,
        deadline: goal.deadline ?? '',
      });
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      targetAmount: [null, [Validators.required, Validators.min(0.01)]],
      deadline: [''],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;
    this.errorMessage = '';

    const title = this.form.controls['title'].value as string;
    const targetAmount = Number(this.form.controls['targetAmount'].value);
    const deadlineRaw = (this.form.controls['deadline'].value as string) || '';
    const deadline = deadlineRaw ? deadlineRaw : undefined;

    const request = { title, targetAmount, deadline };

    const action$ = this.editing
      ? this.goalStore.update(this.editing.id, request)
      : this.goalStore.create(request);

    action$.subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/goals']);
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'Failed to save goal. Please try again.';
      },
    });
  }

  onCancel(): void {
    this.router.navigate(['/goals']);
  }
}
