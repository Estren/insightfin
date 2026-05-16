import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { LoginRequest, RegisterRequest } from '../models/auth.model';
import { AuthService } from '../services/auth.service';
import { decodeJwtPayload } from '../utils/jwt.utils';

const ACCESS_TOKEN_KEY = 'insightfin_access_token';
const REFRESH_TOKEN_KEY = 'insightfin_refresh_token';

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

  isEmailVerified(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    return decodeJwtPayload(token)['email_verified'] === true;
  }

  getEmailFromToken(): string | null {
    const token = this.getAccessToken();
    if (!token) return null;
    return (decodeJwtPayload(token)['email'] as string) ?? null;
  }

  login(request: LoginRequest): Observable<void> {
    return this.authService.login(request).pipe(
      tap((response) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
        this._isAuthenticated$.next(true);
      }),
      map(() => void 0),
    );
  }

  register(request: RegisterRequest): Observable<void> {
    return this.authService.register(request).pipe(
      tap((response) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
        this._isAuthenticated$.next(true);
      }),
      map(() => void 0),
    );
  }

  logout(): void {
    this.authService.logout().subscribe({
      complete: () => this.clearTokens(),
      error: () => this.clearTokens(),
    });
  }

  saveTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  clearTokens(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this._isAuthenticated$.next(false);
  }
}
