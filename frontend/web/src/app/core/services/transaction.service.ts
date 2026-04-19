import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateTransactionRequest,
  TransactionResponse,
  UpdateTransactionRequest,
} from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(startDate: string, endDate: string): Observable<TransactionResponse[]> {
    const params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    return this.http.get<TransactionResponse[]>(`${this.apiUrl}/transactions`, { params });
  }

  create(request: CreateTransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.apiUrl}/transactions`, request);
  }

  update(id: string, request: UpdateTransactionRequest): Observable<TransactionResponse> {
    return this.http.put<TransactionResponse>(`${this.apiUrl}/transactions/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/transactions/${id}`);
  }
}
