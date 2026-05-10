import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { UserStore } from '../../../../core/stores/user.store';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  imports: [ReactiveFormsModule, AsyncPipe, TranslateModule],
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  profileSubmitting = false;
  passwordSubmitting = false;

  deleteConfirmText = '';
  avatarUploading = false;

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

    this.userStore.update(this.profileForm.value).subscribe({
      next: () => {
        this.profileSubmitting = false;
      },
      error: () => {
        this.profileSubmitting = false;
      },
    });
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) return;
    this.passwordSubmitting = true;

    this.userStore.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.passwordSubmitting = false;
        this.passwordForm.reset();
      },
      error: () => {
        this.passwordSubmitting = false;
      },
    });
  }

  getInitials(): string {
    const name = this.userStore.profile?.name ?? '';
    return name
      .split(' ')
      .map((n) => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  onAvatarSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.avatarUploading = true;
    this.userStore.uploadAvatar(file).subscribe({
      next: () => {
        this.avatarUploading = false;
      },
      error: () => {
        this.avatarUploading = false;
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
