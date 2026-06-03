import { AsyncPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { filter, map, take } from 'rxjs';
import { NotificationStore } from '../../../../core/stores/notification.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { NotificationCardComponent } from '../../../../shared/components/notification-card/notification-card.component';
import { NotificationCardSkeletonComponent } from '../../../../shared/components/notification-card/notification-card-skeleton.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  imports: [
    AsyncPipe,
    TranslateModule,
    CardComponent,
    EmptyStateComponent,
    NotificationCardComponent,
    NotificationCardSkeletonComponent,
    PageHeaderComponent,
  ],
})
export class NotificationListComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);

  readonly focusedId = signal<string | null>(null);

  constructor(public readonly store: NotificationStore) {}

  ngOnInit(): void {
    // Refetch on every entry — keeps the list and the badge in sync after the
    // user marked items read in /feedbacks (which uses a different store).
    this.store.load();
    this.store.loadUnreadCount();

    this.route.queryParamMap.pipe(map((p) => p.get('focus'))).subscribe((id) => {
      this.focusedId.set(id);
      if (!id) return;

      // Wait for the list to include the focused item, then scroll it into view.
      // Two RAFs: first lets Angular flush the `[defaultExpanded]` render so the
      // card has its final height before we center it on screen.
      this.store.notifications$
        .pipe(
          filter((items) => items.some((n) => n.id === id)),
          take(1),
        )
        .subscribe(() => {
          requestAnimationFrame(() => {
            requestAnimationFrame(() => {
              document.getElementById('notification-' + id)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
            });
          });
        });
    });
  }
}
