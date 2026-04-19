import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateGoalContributionRequest,
  CreateGoalRequest,
  GoalContributionResponse,
  GoalResponse,
  UpdateGoalRequest,
} from '../models/goal.model';

@Injectable({ providedIn: 'root' })
export class GoalService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<GoalResponse[]> {
    return this.http.get<GoalResponse[]>(`${this.apiUrl}/goals`);
  }

  create(request: CreateGoalRequest): Observable<GoalResponse> {
    return this.http.post<GoalResponse>(`${this.apiUrl}/goals`, request);
  }

  update(id: string, request: UpdateGoalRequest): Observable<GoalResponse> {
    return this.http.put<GoalResponse>(`${this.apiUrl}/goals/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/goals/${id}`);
  }

  contribute(goalId: string, request: CreateGoalContributionRequest): Observable<GoalContributionResponse> {
    return this.http.post<GoalContributionResponse>(
      `${this.apiUrl}/goals/${goalId}/contributions`,
      request,
    );
  }
}
