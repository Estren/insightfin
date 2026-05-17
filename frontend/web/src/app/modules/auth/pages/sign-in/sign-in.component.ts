import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import {
  GoogleCredentialEvent,
  GoogleSignInButtonComponent,
} from '../../../../shared/components/google-sign-in-button/google-sign-in-button.component';
import { AuthStore } from '../../../../core/stores/auth.store';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css'],
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
export class SignInComponent implements OnInit {
  form!: FormGroup;
  submitted = false;
  passwordTextType!: boolean;
  loading = false;

  constructor(
    private readonly _formBuilder: FormBuilder,
    private readonly _router: Router,
    private readonly _authStore: AuthStore,
    private readonly _toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  get f() {
    return this.form.controls;
  }

  togglePasswordTextType() {
    this.passwordTextType = !this.passwordTextType;
  }

  onSubmit() {
    this.submitted = true;

    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    const { email, password } = this.form.value;

    this._authStore.login({ email, password }).subscribe({
      next: () => {
        this.loading = false;
        this._router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        const key = err.status === 401 ? 'toast.auth.loginError' : 'toast.auth.genericError';
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
