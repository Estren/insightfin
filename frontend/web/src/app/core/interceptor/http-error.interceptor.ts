import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

// Only handles 5xx (server errors). 4xx errors are handled by stores and auth components.
// 401 is handled by authInterceptor (token refresh / redirect).
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status >= 500) {
        toastService.error('toast.generic.error');
      }
      return throwError(() => error);
    }),
  );
};
