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

  readonly confirmButtonClass = computed(() => {
    const variant = this.state()?.variant ?? 'info';
    switch (variant) {
      case 'danger':
        return 'bg-red-600 hover:bg-red-700 text-white';
      case 'warning':
        return 'bg-amber-600 hover:bg-amber-700 text-white';
      default:
        return 'bg-primary hover:bg-primary/90 text-white';
    }
  });

  readonly iconClass = computed(() => {
    const variant = this.state()?.variant ?? 'info';
    switch (variant) {
      case 'danger':
        return 'text-red-600 bg-red-100 dark:bg-red-900/30';
      case 'warning':
        return 'text-amber-600 bg-amber-100 dark:bg-amber-900/30';
      default:
        return 'text-primary bg-primary/10';
    }
  });

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
