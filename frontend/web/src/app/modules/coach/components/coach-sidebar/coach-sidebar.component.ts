import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { CoachThread } from '../../../../core/models/coach.model';
import { ConfirmDialogService } from '../../../../core/services/confirm-dialog.service';
import { CoachStore } from '../../../../core/stores/coach.store';

type GroupKey = 'today' | 'yesterday' | 'lastWeek' | 'older';

interface ThreadGroup {
  key: GroupKey;
  threads: CoachThread[];
}

const GROUP_ORDER: GroupKey[] = ['today', 'yesterday', 'lastWeek', 'older'];
const DAY_MS = 86_400_000;

@Component({
  selector: 'app-coach-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './coach-sidebar.component.html',
  host: { class: 'block h-full min-h-0' },
  imports: [NgClass, FormsModule, TranslateModule],
})
export class CoachSidebarComponent {
  readonly editingId = signal<string | null>(null);
  readonly editingTitle = signal('');
  readonly menuOpenId = signal<string | null>(null);

  /** ChatGPT-style date grouping driven by lastMessageAt. Recomputes whenever
   *  the threads signal changes; doesn't re-trigger on its own across midnight,
   *  which is a refresh-to-fix edge case we tolerate. */
  readonly groupedThreads = computed<ThreadGroup[]>(() => {
    const buckets: Record<GroupKey, CoachThread[]> = {
      today: [],
      yesterday: [],
      lastWeek: [],
      older: [],
    };
    for (const t of this.store.threads()) {
      buckets[this.bucketFor(t.lastMessageAt)].push(t);
    }
    return GROUP_ORDER.filter((k) => buckets[k].length > 0).map((k) => ({ key: k, threads: buckets[k] }));
  });

  constructor(
    readonly store: CoachStore,
    private readonly confirm: ConfirmDialogService,
    private readonly translate: TranslateService,
  ) {}

  relativeLabel(thread: CoachThread): string {
    const days = this.daysAgo(thread.lastMessageAt);
    if (days <= 0) return this.translate.instant('coach.relative.today');
    if (days === 1) return this.translate.instant('coach.relative.yesterday');
    if (days <= 7) return this.translate.instant('coach.relative.daysAgo', { n: days });
    if (days <= 30) return this.translate.instant('coach.relative.weeksAgo', { n: Math.floor(days / 7) });
    return new Date(thread.lastMessageAt).toLocaleDateString(this.translate.currentLang || 'en-US', {
      day: '2-digit',
      month: '2-digit',
    });
  }

  private bucketFor(isoDate: string): GroupKey {
    const days = this.daysAgo(isoDate);
    if (days <= 0) return 'today';
    if (days === 1) return 'yesterday';
    if (days <= 7) return 'lastWeek';
    return 'older';
  }

  private daysAgo(isoDate: string): number {
    const now = new Date();
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const msgDate = new Date(isoDate);
    const startOfMsg = new Date(msgDate.getFullYear(), msgDate.getMonth(), msgDate.getDate()).getTime();
    return Math.round((startOfToday - startOfMsg) / DAY_MS);
  }

  trackById(_: number, t: CoachThread): string {
    return t.id;
  }

  select(id: string): void {
    if (this.editingId()) return;
    void this.store.selectThread(id);
  }

  newConversation(): void {
    this.store.newConversation();
  }

  toggleMenu(event: Event, id: string): void {
    event.stopPropagation();
    this.menuOpenId.set(this.menuOpenId() === id ? null : id);
  }

  startRename(event: Event, thread: CoachThread): void {
    event.stopPropagation();
    this.menuOpenId.set(null);
    this.editingId.set(thread.id);
    this.editingTitle.set(thread.title);
  }

  commitRename(id: string): void {
    const title = this.editingTitle().trim();
    if (title) void this.store.renameThread(id, title);
    this.editingId.set(null);
  }

  cancelRename(): void {
    this.editingId.set(null);
  }

  onRenameKey(event: KeyboardEvent, id: string): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.commitRename(id);
    } else if (event.key === 'Escape') {
      this.cancelRename();
    }
  }

  async remove(event: Event, thread: CoachThread): Promise<void> {
    event.stopPropagation();
    this.menuOpenId.set(null);
    const ok = await firstValueFrom(
      this.confirm.confirm({
        title: this.translate.instant('coach.delete.title'),
        message: this.translate.instant('coach.delete.message', { title: thread.title }),
        confirmLabel: this.translate.instant('coach.delete.confirm'),
        variant: 'danger',
      }),
    );
    if (ok) void this.store.deleteThread(thread.id);
  }
}
