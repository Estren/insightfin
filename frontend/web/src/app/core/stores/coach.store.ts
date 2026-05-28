import { Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { CoachMessage, CoachThread } from '../models/coach.model';
import { CoachService } from '../services/coach.service';

/**
 * Singleton store for the Coach Agent. Holds the thread list (sidebar) plus
 * the messages of the active thread. State lives here, not on the page
 * component, so navigating away and back preserves everything and any
 * in-flight stream keeps running in the background.
 *
 * Threads are persisted server-side (coach_threads); messages live in
 * Foundry and are hydrated on demand when a thread is selected.
 */
@Injectable({ providedIn: 'root' })
export class CoachStore {
  readonly threads = signal<CoachThread[]>([]);
  readonly activeThreadId = signal<string | null>(null);
  readonly messages = signal<CoachMessage[]>([]);

  readonly streaming = signal(false);
  readonly loadingThreads = signal(false);
  readonly loadingMessages = signal(false);
  readonly draft = signal('');
  readonly currentToolCall = signal<string | null>(null);

  private abortController: AbortController | null = null;

  constructor(
    private readonly coachService: CoachService,
    private readonly translate: TranslateService,
  ) {}

  /** Load the sidebar thread list. Called when the page first mounts. */
  async loadThreads(): Promise<void> {
    this.loadingThreads.set(true);
    try {
      const threads = await firstValueFrom(this.coachService.listThreads());
      this.threads.set(threads);
    } catch {
      // Sidebar stays empty; not fatal.
    } finally {
      this.loadingThreads.set(false);
    }
  }

  /** Open an existing thread: set active + hydrate its messages from Foundry. */
  async selectThread(id: string): Promise<void> {
    if (this.activeThreadId() === id) return;
    this.activeThreadId.set(id);
    this.messages.set([]);
    this.loadingMessages.set(true);
    try {
      const messages = await firstValueFrom(this.coachService.getThreadMessages(id));
      // Only apply if the user hasn't switched threads meanwhile.
      if (this.activeThreadId() === id) this.messages.set(messages);
    } catch {
      this.messages.set([]);
    } finally {
      this.loadingMessages.set(false);
    }
  }

  /** Reset to a blank "new conversation" — no DB record until first message. */
  newConversation(): void {
    if (this.streaming()) this.abortController?.abort();
    this.activeThreadId.set(null);
    this.messages.set([]);
    this.draft.set('');
    this.currentToolCall.set(null);
  }

  async ask(question: string): Promise<void> {
    if (this.streaming()) return;

    // Lazy thread creation: the first message of a new conversation creates
    // the persisted thread, then streams into it.
    let threadId = this.activeThreadId();
    if (!threadId) {
      try {
        const thread = await firstValueFrom(this.coachService.createThread(question));
        threadId = thread.id;
        this.activeThreadId.set(thread.id);
        this.threads.update((prev) => [thread, ...prev]);
      } catch {
        this.appendStandaloneError(this.translate.instant('coach.errors.network'));
        return;
      }
    }

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
      for await (const event of this.coachService.streamChat(question, threadId, this.abortController.signal)) {
        switch (event.type) {
          case 'thread':
            // Foundry id — frontend uses our UUID, so ignore.
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
      this.bumpActiveThreadToTop();
    }
  }

  cancel(): void {
    this.abortController?.abort();
  }

  async deleteThread(id: string): Promise<void> {
    try {
      await firstValueFrom(this.coachService.deleteThread(id));
    } catch {
      return; // leave it in the list if the server rejected
    }
    this.threads.update((prev) => prev.filter((t) => t.id !== id));
    if (this.activeThreadId() === id) {
      this.newConversation();
    }
  }

  async renameThread(id: string, title: string): Promise<void> {
    const clean = title.trim();
    if (!clean) return;
    try {
      const updated = await firstValueFrom(this.coachService.renameThread(id, clean));
      this.threads.update((prev) => prev.map((t) => (t.id === id ? updated : t)));
    } catch {
      // keep old title
    }
  }

  private bumpActiveThreadToTop(): void {
    const id = this.activeThreadId();
    if (!id) return;
    this.threads.update((prev) => {
      const idx = prev.findIndex((t) => t.id === id);
      if (idx < 0) return prev;
      const updated = { ...prev[idx], lastMessageAt: new Date().toISOString() };
      const rest = prev.filter((t) => t.id !== id);
      return [updated, ...rest];
    });
  }

  private appendStandaloneError(message: string): void {
    this.messages.update((prev) => [
      ...prev,
      {
        id: this.makeId(),
        role: 'assistant',
        text: `⚠️ ${message}`,
        toolCalls: [],
        citations: [],
        errored: true,
      },
    ]);
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
