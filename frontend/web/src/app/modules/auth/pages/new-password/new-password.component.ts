import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-new-password',
  templateUrl: './new-password.component.html',
  styleUrls: ['./new-password.component.css'],
  imports: [FormsModule, ReactiveFormsModule, RouterLink, AngularSvgIconModule, ButtonComponent, NgClass, TranslateModule],
})
export class NewPasswordComponent implements OnInit {
  form!: FormGroup;
  submitted = false;
  loading = false;
  passwordTextType = false;
  confirmPasswordTextType = false;
  token: string | null = null;

  constructor(
    private readonly _formBuilder: FormBuilder,
    private readonly _route: ActivatedRoute,
    private readonly _router: Router,
    private readonly _authService: AuthService,
    private readonly _toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.token = this._route.snapshot.queryParamMap.get('token');

    this.form = this._formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });
  }

  get f() {
    return this.form.controls;
  }

  togglePasswordTextType(): void {
    this.passwordTextType = !this.passwordTextType;
  }

  toggleConfirmPasswordTextType(): void {
    this.confirmPasswordTextType = !this.confirmPasswordTextType;
  }

  onSubmit(): void {
    this.submitted = true;

    if (!this.token) {
      this._toastService.error('toast.auth.resetInvalid');
      return;
    }

    if (this.form.invalid) {
      return;
    }

    const { password, confirmPassword } = this.form.value;
    if (password !== confirmPassword) {
      this._toastService.error('toast.auth.passwordMismatch');
      return;
    }

    this.loading = true;

    this._authService.resetPassword(this.token, password).subscribe({
      next: () => {
        this.loading = false;
        this._toastService.success('toast.auth.resetSuccess');
        this._router.navigate(['/auth/sign-in']);
      },
      error: (err) => {
        this.loading = false;
        const key = err.status === 400 ? 'toast.auth.resetInvalid' : 'toast.auth.resetError';
        this._toastService.error(key);
      },
    });
  }
}
