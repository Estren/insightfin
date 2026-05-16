import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { UserStore } from '../../../../core/stores/user.store';

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
      },
      error: () => {
        this.loading = false;
        this.error = true;
      },
    });
  }

  goToSignIn(): void {
    this.router.navigate(['/auth/sign-in']);
  }
}
