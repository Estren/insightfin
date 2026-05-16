import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../stores/auth.store';

export const verifiedEmailGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  if (!authStore.getAccessToken()) {
    return router.createUrlTree(['/auth/sign-in']);
  }

  if (!authStore.isEmailVerified()) {
    return router.createUrlTree(['/auth/verify-email']);
  }

  return true;
};
