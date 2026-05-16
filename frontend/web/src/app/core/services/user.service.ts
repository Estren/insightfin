import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChangePasswordRequest, UpdateUserRequest, UserProfile } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/users/me`);
  }

  updateMe(request: UpdateUserRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/users/me`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/users/me/password`, request);
  }

  uploadAvatar(file: File): Observable<UserProfile> {
    const form = new FormData();
    form.append('file', file);
    return this.http.put<UserProfile>(`${this.apiUrl}/users/me/avatar`, form);
  }

  deleteMe(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/me`);
  }

  requestEmailChange(newEmail: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/me/email/change-request`, { newEmail });
  }

  confirmEmailChangeByLink(token: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/me/email/change-confirm`, { token });
  }

  confirmEmailChangeByPin(pin: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/me/email/change-confirm-pin`, { pin });
  }
}
