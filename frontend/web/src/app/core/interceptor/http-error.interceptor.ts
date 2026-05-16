import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

// Only handles 5xx (server errors). 4xx errors are handled by stores and auth components.
// 401 is handled by authInterceptor (token refresh / redirect).
// 403 EMAIL_NOT_VERIFIED is handled here to redirect to the verify-email page.
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 403 && error.error?.error_code === 'EMAIL_NOT_VERIFIED') {
          router.navigate(['/auth/verify-email']);
          return throwError(() => error);
        }
        if (error.status >= 500) {
          toastService.error('toast.generic.error');
        }
      }
      return throwError(() => error);
    }),
  );
};
