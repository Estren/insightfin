import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { CategoryService } from '../services/category.service';

export const ONBOARDING_FLAG = 'insightfin_onboarding_done';

/**
 * Redirects brand-new users (no categories yet, never onboarded on this device)
 * to the onboarding wizard. Once resolved, a localStorage flag short-circuits
 * the check so no extra request runs on later navigations.
 */
export const onboardingGuard: CanActivateFn = () => {
  const router = inject(Router);
  const categoryService = inject(CategoryService);

  if (localStorage.getItem(ONBOARDING_FLAG)) {
    return true;
  }

  return categoryService.list().pipe(
    map((categories) => {
      if (categories.length > 0) {
        localStorage.setItem(ONBOARDING_FLAG, 'true');
        return true;
      }
      return router.createUrlTree(['/onboarding']);
    }),
    // Never block the app if the check itself fails.
    catchError(() => of(true)),
  );
};
