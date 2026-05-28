import { Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { CoachMessage } from '../models/coach.model';
import { CoachService } from '../services/coach.service';

/**
 * Singleton store for the Coach Agent chat. State lives here instead of in
 * the page component so navigating away from /coach and back preserves the
 * conversation (and any in-flight stream keeps running in the background,
 * the same way ChatGPT works — switch tabs and the answer is still there
 * when you come back).
 */
@Injectable({ providedIn: 'root' })
export class CoachStore {
  readonly messages = signal<CoachMessage[]>([]);
  readonly threadId = signal<string | null>(null);
  readonly streaming = signal(false);
  readonly draft = signal('');
  readonly currentToolCall = signal<string | null>(null);

  private abortController: AbortController | null = null;

  constructor(
    private readonly coachService: CoachService,
    private readonly translate: TranslateService,
  ) {}

  async ask(question: string): Promise<void> {
    if (this.streaming()) return;

    const userMsg: CoachMessage = {
      id: this.makeId(),
      role: 'user',
      text: question,
      toolCalls: [],
      citations: [],
    };
    const assistantMsg: CoachMessage = {
      id: this.makeId(),
      role: 'assistant',
      text: '',
      toolCalls: [],
      citations: [],
      isStreaming: true,
    };
    this.messages.update((prev) => [...prev, userMsg, assistantMsg]);
    this.streaming.set(true);
    this.currentToolCall.set(null);

    this.abortController = new AbortController();
    const assistantId = assistantMsg.id;

    try {
      for await (const event of this.coachService.streamChat(question, this.threadId(), this.abortController.signal)) {
        switch (event.type) {
          case 'thread':
            if (event.id) this.threadId.set(event.id);
            break;
          case 'token':
            this.appendToken(assistantId, event.data);
            break;
          case 'tool_call':
            this.currentToolCall.set(event.name);
            this.recordToolCall(assistantId, event.name);
            break;
          case 'citation':
            this.recordCitation(assistantId, event.marker, event.filename);
            break;
          case 'error':
            this.appendToken(assistantId, `\n\n⚠️ ${event.data}`);
            this.markErrored(assistantId);
            break;
          case 'done':
            break;
        }
      }
    } catch (err: unknown) {
      if (!(err instanceof DOMException && err.name === 'AbortError')) {
        this.appendToken(assistantId, `\n\n⚠️ ${this.translate.instant('coach.errors.network')}`);
        this.markErrored(assistantId);
      }
    } finally {
      this.finishStreaming(assistantId);
      this.streaming.set(false);
      this.currentToolCall.set(null);
      this.abortController = null;
    }
  }

  cancel(): void {
    this.abortController?.abort();
  }

  newConversation(): void {
    if (this.streaming()) this.cancel();
    this.messages.set([]);
    this.threadId.set(null);
    this.draft.set('');
    this.currentToolCall.set(null);
  }

  private appendToken(id: string, chunk: string): void {
    this.messages.update((prev) => prev.map((m) => (m.id === id ? { ...m, text: m.text + chunk } : m)));
  }

  private recordToolCall(id: string, name: string): void {
    this.messages.update((prev) => prev.map((m) => (m.id === id ? { ...m, toolCalls: [...m.toolCalls, name] } : m)));
  }

  private recordCitation(id: string, marker: number, filename: string): void {
    this.messages.update((prev) =>
      prev.map((m) => (m.id === id ? { ...m, citations: [...m.citations, { marker, filename }] } : m)),
    );
  }

  private markErrored(id: string): void {
    this.messages.update((prev) => prev.map((m) => (m.id === id ? { ...m, errored: true } : m)));
  }

  private finishStreaming(id: string): void {
    this.messages.update((prev) => prev.map((m) => (m.id === id ? { ...m, isStreaming: false } : m)));
  }

  private makeId(): string {
    return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  }
}
