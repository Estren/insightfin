import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { LoginRequest, RegisterRequest, UserResponse } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'orizon_access_token';
const REFRESH_TOKEN_KEY = 'orizon_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly _isAuthenticated$ = new BehaviorSubject<boolean>(this.hasToken());
  readonly isAuthenticated$ = this._isAuthenticated$.asObservable();

  constructor(private readonly authService: AuthService) {}

  private hasToken(): boolean {
    return !!localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  login(request: LoginRequest): Observable<void> {
    return this.authService.login(request).pipe(
      tap((response) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
        this._isAuthenticated$.next(true);
      }),
      tap(() => {}), // map to void
    ) as unknown as Observable<void>;
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.authService.register(request);
  }

  logout(): void {
    this.authService.logout().subscribe({
      complete: () => this.clearTokens(),
      error: () => this.clearTokens(),
    });
  }

  clearTokens(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this._isAuthenticated$.next(false);
  }
}
