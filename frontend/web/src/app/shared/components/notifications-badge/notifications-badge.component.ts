import { AsyncPipe } from '@angular/common';
import { Component, ElementRef, HostListener, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { map } from 'rxjs';
import { NotificationResponse } from '../../../core/models/notification.model';
import { NotificationStore } from '../../../core/stores/notification.store';
import { NotificationCardComponent } from '../notification-card/notification-card.component';

@Component({
  selector: 'app-notifications-badge',
  templateUrl: './notifications-badge.component.html',
  imports: [AsyncPipe, RouterLink, TranslateModule, AngularSvgIconModule, NotificationCardComponent],
})
export class NotificationsBadgeComponent implements OnInit {
  readonly totalUnread$ = this.store.counts$.pipe(map((c) => c.total));
  readonly open = signal(false);

  constructor(
    public readonly store: NotificationStore,
    private readonly host: ElementRef<HTMLElement>,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.store.loadUnreadCount();
    this.store.loadPreview();
  }

  toggle(): void {
    const willOpen = !this.open();
    this.open.set(willOpen);
    if (willOpen) {
      // Refresh the preview on each open — items the user has marked read elsewhere
      // get the up-to-date read flag.
      this.store.loadPreview();
    }
  }

  close(): void {
    this.open.set(false);
  }

  onSeeAllClick(): void {
    this.close();
    this.router.navigate(['/notifications']);
  }

  onItemClick(notification: NotificationResponse): void {
    this.store.markAsRead(notification);
    this.close();
    this.router.navigate(['/notifications'], { queryParams: { focus: notification.id } });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.open()) return;
    if (!this.host.nativeElement.contains(event.target as Node)) {
      this.close();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.open()) this.close();
  }
}
