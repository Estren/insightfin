import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationResponse, UnreadCountResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.apiUrl}/notifications`);
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
