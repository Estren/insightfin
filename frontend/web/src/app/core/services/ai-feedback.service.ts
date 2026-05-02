import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AiFeedbackResponse } from '../models/ai-feedback.model';

@Injectable({ providedIn: 'root' })
export class AiFeedbackService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(month: string): Observable<AiFeedbackResponse[]> {
    const params = new HttpParams().set('month', month);
    return this.http.get<AiFeedbackResponse[]>(`${this.apiUrl}/feedbacks`, { params });
  }

  getById(id: string): Observable<AiFeedbackResponse> {
    return this.http.get<AiFeedbackResponse>(`${this.apiUrl}/feedbacks/${id}`);
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/feedbacks/${id}/read`, {});
  }
}
