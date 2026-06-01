import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CoachChartData, CoachChartKind, CoachEvent, CoachMessage, CoachThread } from '../models/coach.model';
import { AuthStore } from '../stores/auth.store';

interface ThreadMessageDto {
  role: 'user' | 'assistant';
  text: string;
  citations: { marker: number; filename: string }[];
  createdAt: string | null;
}

/**
 * Coach Agent client.
 *
 * Streaming (`streamChat`) uses fetch + ReadableStream because EventSource
 * doesn't support POST or custom Authorization headers. The thread CRUD
 * uses the regular HttpClient so the JWT interceptor applies automatically.
 *
 * The SSE parser implements the minimal subset we emit on the server:
 * lines starting with `event:` and `data:`, events separated by `\n\n`.
 */
@Injectable({ providedIn: 'root' })
export class CoachService {
  private readonly chatUrl = `${environment.apiUrl}/coach/chat`;
  private readonly threadsUrl = `${environment.apiUrl}/coach/threads`;
  private readonly actionsUrl = `${environment.apiUrl}/coach/actions`;

  constructor(
    private readonly auth: AuthStore,
    private readonly http: HttpClient,
  ) {}

  listThreads(): Observable<CoachThread[]> {
    return this.http.get<CoachThread[]>(this.threadsUrl);
  }

  createThread(firstMessage: string): Observable<CoachThread> {
    return this.http.post<CoachThread>(this.threadsUrl, { firstMessage });
  }

  renameThread(id: string, title: string): Observable<CoachThread> {
    return this.http.patch<CoachThread>(`${this.threadsUrl}/${id}`, { title });
  }

  deleteThread(id: string): Observable<void> {
    return this.http.delete<void>(`${this.threadsUrl}/${id}`);
  }

  /** Execute a Coach-proposed write action the user confirmed. Deterministic, server-side. */
  executeAction(action: string, params: Record<string, unknown>): Observable<{ status: string; summary?: string }> {
    return this.http.post<{ status: string; summary?: string }>(`${this.actionsUrl}/execute`, { action, params });
  }

  getThreadMessages(id: string): Observable<CoachMessage[]> {
    return new Observable<CoachMessage[]>((subscriber) => {
      const sub = this.http.get<ThreadMessageDto[]>(`${this.threadsUrl}/${id}/messages`).subscribe({
        next: (dtos) => {
          subscriber.next(dtos.map((d) => this.toMessage(d)));
          subscriber.complete();
        },
        error: (err) => subscriber.error(err),
      });
      return () => sub.unsubscribe();
    });
  }

  private toMessage(dto: ThreadMessageDto): CoachMessage {
    return {
      id: `${dto.createdAt ?? ''}-${Math.random().toString(36).slice(2, 8)}`,
      role: dto.role,
      text: dto.text,
      toolCalls: [],
      citations: dto.citations ?? [],
    };
  }

  async *streamChat(question: string, threadId: string | null, signal?: AbortSignal): AsyncGenerator<CoachEvent> {
    const token = this.auth.getAccessToken();
    if (!token) {
      yield { type: 'error', data: 'Sessão expirada. Faça login novamente.' };
      return;
    }

    const body: { question: string; threadId?: string } = { question };
    if (threadId) body.threadId = threadId;

    const response = await fetch(this.chatUrl, {
      method: 'POST',
      signal,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const detail = await this.safeReadText(response);
      yield {
        type: 'error',
        data: `HTTP ${response.status}: ${detail || response.statusText}`,
      };
      return;
    }
    if (!response.body) {
      yield { type: 'error', data: 'Resposta sem corpo de streaming.' };
      return;
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    try {
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        // Events end with a blank line (\n\n). Keep the trailing partial in
        // `buffer` until the next chunk completes it.
        const segments = buffer.split('\n\n');
        buffer = segments.pop() ?? '';

        for (const segment of segments) {
          const event = this.parseSegment(segment);
          if (event) yield event;
        }
      }
    } finally {
      reader.releaseLock();
    }
  }

  private parseSegment(segment: string): CoachEvent | null {
    let eventName = 'message';
    let dataLine = '';

    for (const rawLine of segment.split('\n')) {
      const line = rawLine.trimEnd();
      if (!line) continue;
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        dataLine = line.slice(5).trim();
      }
    }

    if (!dataLine) return null;

    let payload: Record<string, unknown> = {};
    try {
      payload = JSON.parse(dataLine);
    } catch {
      return { type: 'error', data: `Resposta SSE inválida: ${dataLine}` };
    }

    switch (eventName) {
      case 'thread':
        return { type: 'thread', id: String(payload['id'] ?? '') };
      case 'token':
        return { type: 'token', data: String(payload['data'] ?? '') };
      case 'tool_call':
        return { type: 'tool_call', name: String(payload['name'] ?? '') };
      case 'tool_executed':
        return {
          type: 'tool_executed',
          name: String(payload['name'] ?? ''),
          args: (payload['args'] as Record<string, unknown>) ?? {},
          result: payload['result'],
        };
      case 'action_proposal':
        return {
          type: 'action_proposal',
          action: String(payload['action'] ?? ''),
          params: (payload['params'] as Record<string, unknown>) ?? {},
          summary: String(payload['summary'] ?? ''),
        };
      case 'chart_payload': {
        const kind = payload['kind'] === 'donut' ? 'donut' : ('line' as CoachChartKind);
        return {
          type: 'chart_payload',
          kind,
          title: String(payload['title'] ?? ''),
          data: (payload['data'] as CoachChartData) ?? ({ categories: [], series: [] } as CoachChartData),
        };
      }
      case 'citation':
        return {
          type: 'citation',
          marker: Number(payload['marker'] ?? 0),
          filename: String(payload['filename'] ?? ''),
        };
      case 'error':
        return { type: 'error', data: String(payload['data'] ?? '') };
      case 'done':
        return { type: 'done' };
      default:
        return null;
    }
  }

  private async safeReadText(response: Response): Promise<string> {
    try {
      return await response.text();
    } catch {
      return '';
    }
  }
}
