import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, throwError } from 'rxjs';
import { catchError, filter, switchMap, take, tap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { AuthStore } from '../stores/auth.store';

let isRefreshing = false;
const refreshToken$ = new BehaviorSubject<string | null>(null);

function withBearer(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authStore.getAccessToken();

  if (token && !req.url.includes('/auth/')) {
    req = withBearer(req, token);
  }

  return next(req).pipe(
    catchError((error) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || req.url.includes('/auth/')) {
        return throwError(() => error);
      }

      if (!isRefreshing) {
        isRefreshing = true;
        refreshToken$.next(null);

        const storedRefreshToken = authStore.getRefreshToken();

        if (!storedRefreshToken) {
          isRefreshing = false;
          authStore.clearTokens();
          router.navigate(['/auth/sign-in']);
          return throwError(() => error);
        }

        return authService.refresh(storedRefreshToken).pipe(
          tap((response) => {
            isRefreshing = false;
            authStore.saveTokens(response.accessToken, response.refreshToken);
            refreshToken$.next(response.accessToken);
          }),
          switchMap((response) => next(withBearer(req, response.accessToken))),
          catchError((refreshError) => {
            isRefreshing = false;
            authStore.clearTokens();
            router.navigate(['/auth/sign-in']);
            return throwError(() => refreshError);
          }),
        );
      }

      // Refresh already in progress — wait for the new token and retry
      return refreshToken$.pipe(
        filter((t) => t !== null),
        take(1),
        switchMap((t) => next(withBearer(req, t!))),
      );
    }),
  );
};
