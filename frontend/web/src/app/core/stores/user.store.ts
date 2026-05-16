import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { ChangePasswordRequest, UpdateUserRequest, UserProfile } from '../models/user.model';
import { UserService } from '../services/user.service';
import { AuthStore } from './auth.store';
import { ToastService } from '../services/toast.service';

@Injectable({ providedIn: 'root' })
export class UserStore {
  private readonly _profile$ = new BehaviorSubject<UserProfile | null>(null);
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  private readonly _error$ = new BehaviorSubject<string>('');

  readonly profile$ = this._profile$.asObservable();
  readonly loading$ = this._loading$.asObservable();
  readonly error$ = this._error$.asObservable();

  constructor(
    private readonly userService: UserService,
    private readonly authStore: AuthStore,
    private readonly toastService: ToastService,
  ) {}

  get profile(): UserProfile | null {
    return this._profile$.value;
  }

  load(): void {
    this._loading$.next(true);
    this._error$.next('');

    this.userService.getMe().subscribe({
      next: (profile) => {
        this._profile$.next(profile);
        this._loading$.next(false);
      },
      error: () => {
        this._error$.next('Failed to load user profile.');
        this._loading$.next(false);
        this.toastService.error('toast.user.loadError');
      },
    });
  }

  update(request: UpdateUserRequest): Observable<UserProfile> {
    return this.userService.updateMe(request).pipe(
      tap((profile) => {
        this._profile$.next(profile);
        this.toastService.success('toast.user.updated');
      }),
      catchError((err) => {
        this.toastService.error('toast.user.updateError');
        return throwError(() => err);
      }),
    );
  }

  uploadAvatar(file: File): Observable<void> {
    return this.userService.uploadAvatar(file).pipe(
      tap((profile) => {
        this._profile$.next(profile);
        this.toastService.success('toast.user.avatarUploaded');
      }),
      map(() => void 0),
      catchError((err) => {
        this.toastService.error('toast.user.avatarError');
        return throwError(() => err);
      }),
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.userService.changePassword(request).pipe(
      tap(() => this.toastService.success('toast.user.passwordChanged')),
      catchError((err) => {
        this.toastService.error('toast.user.passwordError');
        return throwError(() => err);
      }),
    );
  }

  deleteAccount(): Observable<void> {
    return this.userService.deleteMe().pipe(
      tap(() => {
        this._profile$.next(null);
        this.authStore.clearTokens();
        this.toastService.success('toast.user.accountDeleted');
      }),
      catchError((err) => {
        this.toastService.error('toast.user.deleteError');
        return throwError(() => err);
      }),
    );
  }

  requestEmailChange(newEmail: string): Observable<void> {
    return this.userService.requestEmailChange(newEmail).pipe(
      tap(() => this.toastService.success('toast.user.emailChangeSent')),
      catchError((err) => {
        const key = err.status === 409 ? 'toast.auth.emailTaken' : 'toast.user.emailChangeError';
        this.toastService.error(key);
        return throwError(() => err);
      }),
    );
  }

  confirmEmailChangePin(pin: string): Observable<void> {
    return this.userService.confirmEmailChangeByPin(pin).pipe(
      tap(() => {
        this.toastService.success('toast.user.emailChanged');
        this.load();
      }),
      catchError((err) => {
        this.toastService.error('toast.user.emailChangePinError');
        return throwError(() => err);
      }),
    );
  }

  confirmEmailChangeByLink(token: string): Observable<void> {
    return this.userService.confirmEmailChangeByLink(token).pipe(
      tap(() => {
        this.toastService.success('toast.user.emailChanged');
        this.load();
      }),
      catchError((err) => {
        this.toastService.error('toast.user.emailChangeLinkError');
        return throwError(() => err);
      }),
    );
  }

  clear(): void {
    this._profile$.next(null);
    this._error$.next('');
  }
}
