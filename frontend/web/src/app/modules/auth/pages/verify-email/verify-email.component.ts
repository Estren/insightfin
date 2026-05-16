import { NgClass } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { AuthService } from '../../../../core/services/auth.service';
import { AuthStore } from '../../../../core/stores/auth.store';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.css'],
  imports: [NgClass, RouterLink, ButtonComponent, TranslateModule],
})
export class VerifyEmailComponent implements OnInit, OnDestroy {
  @ViewChildren('pinInput') pinInputs!: QueryList<ElementRef<HTMLInputElement>>;

  digits = ['', '', '', '', '', ''];
  email = '';
  loading = false;
  linkError = false;
  cooldown = 0;

  private cooldownInterval?: ReturnType<typeof setInterval>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly authStore: AuthStore,
    private readonly toastService: ToastService,
  ) {}

  ngOnInit(): void {
    if (this.authStore.isEmailVerified()) {
      this.router.navigate(['/']);
      return;
    }

    this.email = this.authStore.getEmailFromToken() ?? '';

    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.verifyByLink(token);
    }
  }

  ngOnDestroy(): void {
    clearInterval(this.cooldownInterval);
  }

  get pin(): string {
    return this.digits.join('');
  }

  onDigitInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '').slice(-1);
    this.digits[index] = value;
    input.value = value;

    if (value && index < 5) {
      this.pinInputs.toArray()[index + 1].nativeElement.focus();
    }

    if (this.pin.length === 6) {
      this.verifyByPin();
    }
  }

  onDigitKeydown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace' && !this.digits[index] && index > 0) {
      this.digits[index - 1] = '';
      this.pinInputs.toArray()[index - 1].nativeElement.focus();
    }
  }

  onPaste(event: ClipboardEvent): void {
    const text = event.clipboardData?.getData('text') ?? '';
    const nums = text.replace(/\D/g, '').slice(0, 6).split('');
    nums.forEach((d, i) => (this.digits[i] = d));
    event.preventDefault();
    const inputs = this.pinInputs.toArray();
    const lastFilled = Math.min(nums.length, 5);
    inputs[lastFilled].nativeElement.focus();
    if (this.pin.length === 6) {
      this.verifyByPin();
    }
  }

  verifyByPin(): void {
    if (this.pin.length < 6 || this.loading) return;
    this.loading = true;

    this.authService.verifyEmailByPin(this.email, this.pin).subscribe({
      next: () => this.onVerifySuccess(),
      error: () => {
        this.loading = false;
        this.toastService.error('toast.auth.verifyPinError');
        this.digits = ['', '', '', '', '', ''];
        setTimeout(() => this.pinInputs.toArray()[0]?.nativeElement.focus(), 0);
      },
    });
  }

  resend(): void {
    if (this.cooldown > 0 || !this.email) return;

    this.authService.resendVerification(this.email).subscribe({
      next: () => {
        this.toastService.success('toast.auth.verifyResent');
        this.startCooldown();
      },
      error: () => this.toastService.error('toast.auth.verifyResendError'),
    });
  }

  private verifyByLink(token: string): void {
    this.loading = true;
    this.authService.verifyEmailByLink(token).subscribe({
      next: () => this.onVerifySuccess(),
      error: () => {
        this.loading = false;
        this.linkError = true;
      },
    });
  }

  private onVerifySuccess(): void {
    const refreshToken = this.authStore.getRefreshToken();
    if (!refreshToken) {
      this.router.navigate(['/auth/sign-in']);
      return;
    }

    this.authService.refresh(refreshToken).subscribe({
      next: (response) => {
        this.authStore.saveTokens(response.accessToken, response.refreshToken);
        this.toastService.success('toast.auth.verifySuccess');
        this.router.navigate(['/']);
      },
      error: () => {
        this.authStore.clearTokens();
        this.router.navigate(['/auth/sign-in']);
      },
    });
  }

  private startCooldown(): void {
    this.cooldown = 60;
    this.cooldownInterval = setInterval(() => {
      this.cooldown--;
      if (this.cooldown <= 0) {
        clearInterval(this.cooldownInterval);
      }
    }, 1000);
  }
}
