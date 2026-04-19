import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  get(month: string): Observable<DashboardResponse> {
    const params = new HttpParams().set('month', month);
    return this.http.get<DashboardResponse>(`${this.apiUrl}/dashboard`, { params });
  }
}
