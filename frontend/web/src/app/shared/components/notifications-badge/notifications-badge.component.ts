import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { map } from 'rxjs';
import { NotificationStore } from '../../../core/stores/notification.store';

@Component({
  selector: 'app-notifications-badge',
  templateUrl: './notifications-badge.component.html',
  imports: [AsyncPipe, RouterLink, TranslateModule, AngularSvgIconModule],
})
export class NotificationsBadgeComponent implements OnInit {
  readonly totalUnread$ = this.store.counts$.pipe(map((c) => c.total));

  constructor(public readonly store: NotificationStore) {}

  ngOnInit(): void {
    this.store.loadUnreadCount();
  }
}
