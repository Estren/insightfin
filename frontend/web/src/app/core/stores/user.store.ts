import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { ChangePasswordRequest, UpdateUserRequest, UserProfile } from '../models/user.model';
import { UserService } from '../services/user.service';
import { AuthStore } from './auth.store';

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
      },
    });
  }

  update(request: UpdateUserRequest): Observable<UserProfile> {
    return this.userService.updateMe(request).pipe(
      tap((profile) => this._profile$.next(profile)),
    );
  }

  uploadAvatar(file: File): Observable<void> {
    return this.userService.uploadAvatar(file).pipe(
      tap((profile) => this._profile$.next(profile)),
      map(() => void 0),
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.userService.changePassword(request);
  }

  deleteAccount(): Observable<void> {
    return this.userService.deleteMe().pipe(
      tap(() => {
        this._profile$.next(null);
        this.authStore.clearTokens();
      }),
    );
  }

  clear(): void {
    this._profile$.next(null);
    this._error$.next('');
  }
}
