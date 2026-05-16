import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { UserStore } from '../../../../core/stores/user.store';
import { AuthStore } from '../../../../core/stores/auth.store';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-confirm-email-change',
  templateUrl: './confirm-email-change.component.html',
  imports: [RouterLink, ButtonComponent, TranslateModule],
})
export class ConfirmEmailChangeComponent implements OnInit {
  loading = true;
  success = false;
  error = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly userStore: UserStore,
    private readonly authStore: AuthStore,
    private readonly authService: AuthService,
    private readonly toastService: ToastService,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.loading = false;
      this.error = true;
      return;
    }

    this.userStore.confirmEmailChangeByLink(token).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        this.refreshTokenSilently();
      },
      error: () => {
        this.loading = false;
        this.error = true;
      },
    });
  }

  goToDashboard(): void {
    this.router.navigate(['/']);
  }

  private refreshTokenSilently(): void {
    const refreshToken = this.authStore.getRefreshToken();
    if (!refreshToken) return;

    this.authService.refresh(refreshToken).subscribe({
      next: (response) => this.authStore.saveTokens(response.accessToken, response.refreshToken),
      error: () => {},
    });
  }
}
