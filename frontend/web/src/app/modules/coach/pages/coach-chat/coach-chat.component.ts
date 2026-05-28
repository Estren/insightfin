import { NgClass } from '@angular/common';
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
import { CoachMessage, CoachSuggestion } from '../../../../core/models/coach.model';
import { CoachStore } from '../../../../core/stores/coach.store';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
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
  imports: [NgClass, FormsModule, TranslateModule, PageHeaderComponent, CoachSidebarComponent],
})
export class CoachChatComponent implements OnInit, AfterViewChecked {
  readonly suggestions = SUGGESTIONS;

  /** Mobile drawer state — sidebar overlays on small screens. */
  readonly sidebarOpen = signal(false);

  private shouldScroll = true;

  @ViewChild('scrollContainer') scrollContainer?: ElementRef<HTMLElement>;

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

  ngOnInit(): void {
    void this.store.loadThreads();
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
    void this.store.ask(text);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.submitDraft();
    }
  }

  onInput(event: Event): void {
    this.store.draft.set((event.target as HTMLTextAreaElement).value);
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
}
