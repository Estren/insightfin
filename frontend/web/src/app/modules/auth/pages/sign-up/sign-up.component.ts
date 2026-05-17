import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import {
  GoogleCredentialEvent,
  GoogleSignInButtonComponent,
} from 'src/app/shared/components/google-sign-in-button/google-sign-in-button.component';
import { AuthStore } from '../../../../core/stores/auth.store';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.css'],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    AngularSvgIconModule,
    ButtonComponent,
    GoogleSignInButtonComponent,
    NgClass,
    TranslateModule,
  ],
})
export class SignUpComponent implements OnInit {
  form!: FormGroup;
  submitted = false;
  loading = false;

  constructor(
    private readonly _formBuilder: FormBuilder,
    private readonly _router: Router,
    private readonly _authStore: AuthStore,
    private readonly _toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit() {
    this.submitted = true;

    if (this.form.invalid) {
      return;
    }

    const { name, email, password, confirmPassword } = this.form.value;

    if (password !== confirmPassword) {
      this._toastService.error('toast.auth.passwordMismatch');
      return;
    }

    this.loading = true;

    this._authStore.register({ name, email, password }).subscribe({
      next: () => {
        this.loading = false;
        this._router.navigate(['/auth/verify-email']);
      },
      error: (err) => {
        this.loading = false;
        const key = err.status === 409 ? 'toast.auth.emailTaken' : 'toast.auth.registerError';
        this._toastService.error(key);
      },
    });
  }

  onGoogleCredential(event: GoogleCredentialEvent): void {
    this._authStore.googleSignIn(event.credential, event.nonce).subscribe({
      next: () => this._router.navigate(['/']),
      error: () => this._toastService.error('toast.auth.googleSignInError'),
    });
  }
}
