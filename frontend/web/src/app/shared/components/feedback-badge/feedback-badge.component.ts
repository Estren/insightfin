import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { AiFeedbackStore } from '../../../core/stores/ai-feedback.store';

@Component({
  selector: 'app-feedback-badge',
  templateUrl: './feedback-badge.component.html',
  imports: [AsyncPipe, RouterLink, TranslateModule, AngularSvgIconModule],
})
export class FeedbackBadgeComponent implements OnInit {
  constructor(public readonly feedbackStore: AiFeedbackStore) {}

  ngOnInit(): void {
    this.feedbackStore.loadUnreadCount();
  }
}
