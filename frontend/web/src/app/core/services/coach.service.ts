import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { CoachEvent } from '../models/coach.model';
import { AuthStore } from '../stores/auth.store';

/**
 * Coach Agent client. Uses fetch + ReadableStream because EventSource
 * does not support POST or custom Authorization headers — both of which
 * we need to send the question and authenticate the request.
 *
 * The parser implements the minimal subset of SSE we emit on the server:
 * lines starting with `event:` and `data:`, events separated by `\n\n`.
 * Each `data:` payload is JSON, parsed once before yielding.
 */
@Injectable({ providedIn: 'root' })
export class CoachService {
  private readonly url = `${environment.apiUrl}/coach/chat`;

  constructor(private readonly auth: AuthStore) {}

  async *streamChat(question: string, threadId: string | null, signal?: AbortSignal): AsyncGenerator<CoachEvent> {
    const token = this.auth.getAccessToken();
    if (!token) {
      yield { type: 'error', data: 'Sessão expirada. Faça login novamente.' };
      return;
    }

    const body: { question: string; threadId?: string } = { question };
    if (threadId) body.threadId = threadId;

    const response = await fetch(this.url, {
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
