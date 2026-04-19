import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BudgetResponse,
  BudgetStatusResponse,
  CreateBudgetRequest,
  UpdateBudgetRequest,
} from '../models/budget.model';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(month: string): Observable<BudgetResponse[]> {
    const params = new HttpParams().set('month', month);
    return this.http.get<BudgetResponse[]>(`${this.apiUrl}/budgets`, { params });
  }

  getStatus(month: string): Observable<BudgetStatusResponse[]> {
    const params = new HttpParams().set('month', month);
    return this.http.get<BudgetStatusResponse[]>(`${this.apiUrl}/budgets/status`, { params });
  }

  create(request: CreateBudgetRequest): Observable<BudgetResponse> {
    return this.http.post<BudgetResponse>(`${this.apiUrl}/budgets`, request);
  }

  update(id: string, request: UpdateBudgetRequest): Observable<BudgetResponse> {
    return this.http.put<BudgetResponse>(`${this.apiUrl}/budgets/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/budgets/${id}`);
  }
}
