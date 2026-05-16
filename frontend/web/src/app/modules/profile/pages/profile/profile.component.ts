import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { UserStore } from '../../../../core/stores/user.store';
import { AuthStore } from '../../../../core/stores/auth.store';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  imports: [ReactiveFormsModule, AsyncPipe, TranslateModule, RouterLink],
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
    public readonly authStore: AuthStore,
  ) {}

  emailChangeForm!: FormGroup;
  emailChangeSubmitting = false;
  emailChangePinDigits = ['', '', '', '', '', ''];
  emailChangePinSubmitting = false;
  showPinForm = false;

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
    });

    this.emailChangeForm = this.fb.group({
      newEmail: ['', [Validators.required, Validators.email]],
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
      this.profileForm.patchValue({ name: profile.name });
    } else {
      this.userStore.profile$.subscribe((p) => {
        if (p) this.profileForm.patchValue({ name: p.name });
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

  onRequestEmailChange(): void {
    if (this.emailChangeForm.invalid) return;
    this.emailChangeSubmitting = true;

    this.userStore.requestEmailChange(this.emailChangeForm.value.newEmail).subscribe({
      next: () => {
        this.emailChangeSubmitting = false;
        this.showPinForm = true;
        this.emailChangeForm.reset();
      },
      error: () => {
        this.emailChangeSubmitting = false;
      },
    });
  }

  onEmailChangePinInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '').slice(-1);
    this.emailChangePinDigits[index] = value;
    input.value = value;
    if (value && index < 5) {
      const inputs = document.querySelectorAll<HTMLInputElement>('.email-change-pin-input');
      inputs[index + 1]?.focus();
    }
    if (this.emailChangePinDigits.join('').length === 6) {
      this.onConfirmEmailChangePin();
    }
  }

  onEmailChangePinKeydown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace' && !this.emailChangePinDigits[index] && index > 0) {
      this.emailChangePinDigits[index - 1] = '';
      const inputs = document.querySelectorAll<HTMLInputElement>('.email-change-pin-input');
      inputs[index - 1]?.focus();
    }
  }

  onConfirmEmailChangePin(): void {
    const pin = this.emailChangePinDigits.join('');
    if (pin.length < 6 || this.emailChangePinSubmitting) return;
    this.emailChangePinSubmitting = true;

    this.userStore.confirmEmailChangePin(pin).subscribe({
      next: () => {
        this.emailChangePinSubmitting = false;
        this.showPinForm = false;
        this.emailChangePinDigits = ['', '', '', '', '', ''];
      },
      error: () => {
        this.emailChangePinSubmitting = false;
        this.emailChangePinDigits = ['', '', '', '', '', ''];
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
