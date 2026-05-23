import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationStore } from '../../../../core/stores/notification.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { NotificationCardComponent } from '../../../../shared/components/notification-card/notification-card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  imports: [
    AsyncPipe,
    TranslateModule,
    CardComponent,
    EmptyStateComponent,
    LoadingComponent,
    NotificationCardComponent,
    PageHeaderComponent,
  ],
})
export class NotificationListComponent implements OnInit {
  constructor(public readonly store: NotificationStore) {}

  ngOnInit(): void {
    // Refetch on every entry — keeps the list and the badge in sync after the
    // user marked items read in /feedbacks (which uses a different store).
    this.store.load();
    this.store.loadUnreadCount();
  }
}
