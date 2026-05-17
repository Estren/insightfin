import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateRecurringTransactionRequest,
  RecurringTransactionResponse,
  UpdateRecurringTransactionRequest,
} from '../models/recurring-transaction.model';

@Injectable({ providedIn: 'root' })
export class RecurringTransactionService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<RecurringTransactionResponse[]> {
    return this.http.get<RecurringTransactionResponse[]>(`${this.apiUrl}/recurring-transactions`);
  }

  create(request: CreateRecurringTransactionRequest): Observable<RecurringTransactionResponse> {
    return this.http.post<RecurringTransactionResponse>(`${this.apiUrl}/recurring-transactions`, request);
  }

  update(id: string, request: UpdateRecurringTransactionRequest): Observable<RecurringTransactionResponse> {
    return this.http.put<RecurringTransactionResponse>(`${this.apiUrl}/recurring-transactions/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/recurring-transactions/${id}`);
  }

  pause(id: string): Observable<RecurringTransactionResponse> {
    return this.http.post<RecurringTransactionResponse>(`${this.apiUrl}/recurring-transactions/${id}/pause`, {});
  }

  resume(id: string): Observable<RecurringTransactionResponse> {
    return this.http.post<RecurringTransactionResponse>(`${this.apiUrl}/recurring-transactions/${id}/resume`, {});
  }
}
