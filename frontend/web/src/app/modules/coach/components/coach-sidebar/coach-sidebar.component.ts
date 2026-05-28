import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { CoachThread } from '../../../../core/models/coach.model';
import { ConfirmDialogService } from '../../../../core/services/confirm-dialog.service';
import { CoachStore } from '../../../../core/stores/coach.store';

@Component({
  selector: 'app-coach-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './coach-sidebar.component.html',
  imports: [NgClass, FormsModule, TranslateModule],
})
export class CoachSidebarComponent {
  readonly editingId = signal<string | null>(null);
  readonly editingTitle = signal('');
  readonly menuOpenId = signal<string | null>(null);

  constructor(
    readonly store: CoachStore,
    private readonly confirm: ConfirmDialogService,
    private readonly translate: TranslateService,
  ) {}

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
