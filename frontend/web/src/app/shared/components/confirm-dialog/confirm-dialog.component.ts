import { Component, computed, inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  imports: [TranslateModule, AngularSvgIconModule],
})
export class ConfirmDialogComponent {
  private readonly service = inject(ConfirmDialogService);

  readonly state = this.service.state;

  // Palette-only: every variant uses primary; the warning/danger intent is
  // carried by the exclamation icon (see iconSrc) and the dialog copy.
  readonly confirmButtonClass = 'bg-primary hover:bg-primary/90 text-white';

  readonly iconClass = 'text-primary bg-primary/10';

  readonly iconSrc = computed(() => {
    const variant = this.state()?.variant ?? 'info';
    return variant === 'info'
      ? 'assets/icons/heroicons/outline/information-circle.svg'
      : 'assets/icons/heroicons/outline/exclamation-triangle.svg';
  });

  confirm(): void {
    this.service.resolve(true);
  }

  cancel(): void {
    this.service.resolve(false);
  }
}
