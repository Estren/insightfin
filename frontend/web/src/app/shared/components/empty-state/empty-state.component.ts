import { Component, input } from '@angular/core';
import { AngularSvgIconModule } from 'angular-svg-icon';

@Component({
  selector: 'app-empty-state',
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.css',
  imports: [AngularSvgIconModule],
})
export class EmptyStateComponent {
  title = input.required<string>();
  description = input<string>('');
  /** Optional heroicon path; when set, renders in a muted circle above the title. */
  icon = input<string>('');
}
