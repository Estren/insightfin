import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '../models/category.model';
import { TransactionType } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(type?: TransactionType): Observable<CategoryResponse[]> {
    let params = new HttpParams();
    if (type) {
      params = params.set('type', type);
    }
    return this.http.get<CategoryResponse[]>(`${this.apiUrl}/categories`, { params });
  }

  create(request: CreateCategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(`${this.apiUrl}/categories`, request);
  }

  update(id: string, request: UpdateCategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.apiUrl}/categories/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }
}
