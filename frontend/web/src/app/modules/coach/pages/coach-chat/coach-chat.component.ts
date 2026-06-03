import { JsonPipe, NgClass } from '@angular/common';
import {
  AfterViewChecked,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  effect,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CoachMessage, CoachSuggestion, CoachToolExecution } from '../../../../core/models/coach.model';
import { CoachStore } from '../../../../core/stores/coach.store';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../../../shared/components/skeleton/skeleton.component';
import { CoachChartComponent } from '../../components/coach-chart/coach-chart.component';
import { CoachSidebarComponent } from '../../components/coach-sidebar/coach-sidebar.component';

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
  imports: [
    JsonPipe,
    NgClass,
    FormsModule,
    TranslateModule,
    PageHeaderComponent,
    SkeletonComponent,
    CoachSidebarComponent,
    CoachChartComponent,
  ],
})
export class CoachChatComponent implements OnInit, AfterViewChecked {
  readonly suggestions = SUGGESTIONS;

  /** Mobile drawer state — sidebar overlays on small screens. */
  readonly sidebarOpen = signal(false);

  private shouldScroll = true;

  @ViewChild('scrollContainer') scrollContainer?: ElementRef<HTMLElement>;
  @ViewChild('composer') composer?: ElementRef<HTMLTextAreaElement>;

  constructor(
    readonly store: CoachStore,
    private readonly translate: TranslateService,
  ) {
    // Any change to the conversation state should re-scroll on next checked.
    effect(() => {
      void this.store.messages();
      void this.store.streaming();
      this.shouldScroll = true;
    });
  }

  async ngOnInit(): Promise<void> {
    await this.store.loadThreads();

    // Deep-link from a dashboard "Pergunte ao Coach" card: start a fresh
    // conversation with the pre-filled question. Clear the state afterwards
    // so a page refresh doesn't re-send it.
    const incoming = (history.state as { question?: unknown } | null)?.question;
    if (typeof incoming === 'string' && incoming.trim()) {
      history.replaceState({ ...history.state, question: null }, '');
      this.store.newConversation();
      void this.store.ask(incoming.trim());
      return;
    }

    // No deep-link, but the user already has conversations: open the most
    // recent one (threads are sorted lastMessageAt DESC server-side). Without
    // this the user always lands on the "Where to start?" empty state, even
    // when their last chat is one click away.
    const threads = this.store.threads();
    if (threads.length > 0 && !this.store.activeThreadId()) {
      void this.store.selectThread(threads[0].id);
    }
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.scrollContainer) {
      const el = this.scrollContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScroll = false;
    }
  }

  trackById(_: number, m: CoachMessage): string {
    return m.id;
  }

  submitDraft(): void {
    const text = this.store.draft().trim();
    if (!text || this.store.streaming()) return;
    this.store.draft.set('');
    // Collapse the textarea back to one line; CSS min-h does the rest.
    if (this.composer) this.composer.nativeElement.style.height = 'auto';
    void this.store.ask(text);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.submitDraft();
    }
  }

  onInput(event: Event): void {
    const ta = event.target as HTMLTextAreaElement;
    this.store.draft.set(ta.value);
    // Auto-grow: shrink first so removing a line actually shrinks the box,
    // then let scrollHeight set the new height. CSS max-h caps the growth
    // and kicks in the internal scroll once the content goes past it.
    ta.style.height = 'auto';
    ta.style.height = `${ta.scrollHeight}px`;
  }

  applySuggestion(s: CoachSuggestion): void {
    if (this.store.streaming()) return;
    const question = this.translate.instant(s.questionKey);
    void this.store.ask(question);
  }

  cancel(): void {
    this.store.cancel();
  }

  newConversation(): void {
    this.store.newConversation();
  }

  /**
   * Turn a raw tool execution like `get_health_score({"month":"2026-06"})` into
   * something the end user can read: "Consultou seu health score · jun/2026".
   * Falls back to the raw function-call form when the tool isn't in the
   * translation map (so new tools degrade gracefully instead of disappearing).
   */
  humanizeToolCall(exec: CoachToolExecution): string {
    const key = `coach.tools.${exec.name}`;
    const params = this.formatToolArgs(exec.args);
    const translated = this.translate.instant(key, params);
    if (translated === key) {
      return `${exec.name}(${JSON.stringify(exec.args)})`;
    }
    return translated;
  }

  /**
   * Per-tool reduction of the raw result JSON into a flat list of label/value
   * rows that render as a tidy mini-table. Returning an empty array signals
   * the template to fall back to the JSON `<pre>` view — so untyped tools
   * still show their data, just less polished.
   */
  summarizeToolResult(exec: CoachToolExecution): { label: string; value: string }[] {
    const fieldLabel = (k: string) => this.translate.instant(`coach.tools.fields.${k}`);

    switch (exec.name) {
      case 'get_health_score': {
        const r = exec.result as { score?: number; breakdown?: Record<string, number> } | null;
        if (!r || typeof r.score !== 'number') return [];
        const rows: { label: string; value: string }[] = [{ label: fieldLabel('score'), value: `${r.score}/100` }];
        const b = r.breakdown ?? {};
        for (const k of ['savingsRate', 'budgetAdherence', 'goalProgress', 'expenseConsistency']) {
          if (typeof b[k] === 'number') rows.push({ label: fieldLabel(k), value: `${b[k]}%` });
        }
        return rows;
      }
      case 'get_budget_status': {
        const r = exec.result as Array<{ categoryName?: string; percentageUsed?: number }> | null;
        if (!Array.isArray(r) || r.length === 0) return [];
        return r.slice(0, 6).map((s) => ({
          label: s.categoryName ?? '—',
          value: `${Math.round(s.percentageUsed ?? 0)}%`,
        }));
      }
      default:
        return [];
    }
  }

  private formatToolArgs(args: Record<string, unknown>): Record<string, string> {
    const out: Record<string, string> = {};
    for (const [k, v] of Object.entries(args ?? {})) {
      out[k] = typeof v === 'string' && /^\d{4}-\d{2}$/.test(v) ? this.formatMonth(v) : String(v ?? '');
    }
    return out;
  }

  private formatMonth(yyyymm: string): string {
    const [y, m] = yyyymm.split('-').map(Number);
    if (!y || !m) return yyyymm;
    return new Date(y, m - 1, 1).toLocaleDateString(this.translate.currentLang || 'en-US', {
      month: 'short',
      year: 'numeric',
    });
  }
}
