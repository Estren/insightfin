import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserStore } from '../../../../core/stores/user.store';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  imports: [ReactiveFormsModule, AsyncPipe],
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  profileSubmitting = false;
  profileSuccess = false;
  profileError = '';

  passwordSubmitting = false;
  passwordSuccess = false;
  passwordError = '';

  deleteConfirmText = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    public readonly userStore: UserStore,
  ) {}

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
    });

    if (!this.userStore.profile) {
      this.userStore.load();
    }

    const profile = this.userStore.profile;
    if (profile) {
      this.profileForm.patchValue({ name: profile.name, email: profile.email });
    } else {
      this.userStore.profile$.subscribe((p) => {
        if (p) this.profileForm.patchValue({ name: p.name, email: p.email });
      });
    }
  }

  onUpdateProfile(): void {
    if (this.profileForm.invalid) return;
    this.profileSubmitting = true;
    this.profileSuccess = false;
    this.profileError = '';

    this.userStore.update(this.profileForm.value).subscribe({
      next: () => {
        this.profileSubmitting = false;
        this.profileSuccess = true;
      },
      error: () => {
        this.profileSubmitting = false;
        this.profileError = 'Failed to update profile. Please try again.';
      },
    });
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) return;
    this.passwordSubmitting = true;
    this.passwordSuccess = false;
    this.passwordError = '';

    this.userStore.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.passwordSubmitting = false;
        this.passwordSuccess = true;
        this.passwordForm.reset();
      },
      error: (err) => {
        this.passwordSubmitting = false;
        this.passwordError =
          err?.status === 400 ? 'Current password is incorrect.' : 'Failed to change password. Please try again.';
      },
    });
  }

  onDeleteAccount(): void {
    if (this.deleteConfirmText !== 'DELETE') return;
    this.userStore.deleteAccount().subscribe({
      next: () => this.router.navigate(['/auth']),
      error: () => {},
    });
  }
}
