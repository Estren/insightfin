import { NgClass } from '@angular/common';
import {
  AfterViewChecked,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CoachMessage, CoachSuggestion } from '../../../../core/models/coach.model';
import { CoachService } from '../../../../core/services/coach.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

const SUGGESTIONS: CoachSuggestion[] = [
  { labelKey: 'coach.suggestions.healthScore.label', questionKey: 'coach.suggestions.healthScore.question' },
  { labelKey: 'coach.suggestions.compareMonths.label', questionKey: 'coach.suggestions.compareMonths.question' },
  { labelKey: 'coach.suggestions.canSpend.label', questionKey: 'coach.suggestions.canSpend.question' },
  { labelKey: 'coach.suggestions.goalPriority.label', questionKey: 'coach.suggestions.goalPriority.question' },
];

@Component({
  selector: 'app-coach-chat',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './coach-chat.component.html',
  imports: [NgClass, FormsModule, TranslateModule, PageHeaderComponent],
})
export class CoachChatComponent implements AfterViewChecked, OnDestroy {
  readonly suggestions = SUGGESTIONS;

  readonly messages = signal<CoachMessage[]>([]);
  readonly streaming = signal(false);
  readonly draft = signal('');
  readonly currentToolCall = signal<string | null>(null);
  readonly threadId = signal<string | null>(null);

  private abortController: AbortController | null = null;
  private shouldScroll = false;

  @ViewChild('scrollContainer') scrollContainer?: ElementRef<HTMLElement>;

  constructor(
    private readonly coach: CoachService,
    private readonly translate: TranslateService,
  ) {}

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.scrollContainer) {
      const el = this.scrollContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScroll = false;
    }
  }

  ngOnDestroy(): void {
    this.abortController?.abort();
  }

  trackById(_: number, m: CoachMessage): string {
    return m.id;
  }

  submitDraft(): void {
    const text = this.draft().trim();
    if (!text || this.streaming()) return;
    this.draft.set('');
    void this.ask(text);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.submitDraft();
    }
  }

  onInput(event: Event): void {
    this.draft.set((event.target as HTMLTextAreaElement).value);
  }

  applySuggestion(s: CoachSuggestion): void {
    if (this.streaming()) return;
    const question = this.translate.instant(s.questionKey);
    void this.ask(question);
  }

  cancel(): void {
    this.abortController?.abort();
  }

  newConversation(): void {
    if (this.streaming()) this.abortController?.abort();
    this.messages.set([]);
    this.threadId.set(null);
    this.draft.set('');
    this.currentToolCall.set(null);
  }

  private async ask(question: string): Promise<void> {
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
    this.shouldScroll = true;

    this.abortController = new AbortController();

    try {
      for await (const event of this.coach.streamChat(question, this.threadId(), this.abortController.signal)) {
        switch (event.type) {
          case 'thread':
            if (event.id) this.threadId.set(event.id);
            break;
          case 'token':
            this.appendToken(assistantMsg.id, event.data);
            break;
          case 'tool_call':
            this.currentToolCall.set(event.name);
            this.recordToolCall(assistantMsg.id, event.name);
            break;
          case 'citation':
            this.recordCitation(assistantMsg.id, event.marker, event.filename);
            break;
          case 'error':
            this.appendToken(assistantMsg.id, `\n\n⚠️ ${event.data}`);
            this.markErrored(assistantMsg.id);
            break;
          case 'done':
            // handled by stream end
            break;
        }
        this.shouldScroll = true;
      }
    } catch (err: unknown) {
      if (!(err instanceof DOMException && err.name === 'AbortError')) {
        this.appendToken(assistantMsg.id, `\n\n⚠️ ${this.translate.instant('coach.errors.network')}`);
        this.markErrored(assistantMsg.id);
      }
    } finally {
      this.finishStreaming(assistantMsg.id);
      this.streaming.set(false);
      this.currentToolCall.set(null);
      this.abortController = null;
    }
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
