import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationPage, UnreadCountResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(limit: number, cursor: string | null): Observable<NotificationPage> {
    let params = new HttpParams().set('limit', String(limit));
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    return this.http.get<NotificationPage>(`${this.apiUrl}/notifications`, { params });
  }

  unreadCount(): Observable<UnreadCountResponse> {
    return this.http.get<UnreadCountResponse>(`${this.apiUrl}/notifications/unread-count`);
  }

  // Read state mutations stay on the type-specific endpoints — the unified API
  // is read-only. The store decides which endpoint to call based on the kind.
  markAiFeedbackAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/feedbacks/${id}/read`, {});
  }

  markBudgetAlertAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/budget-alerts/${id}/read`, {});
  }
}
