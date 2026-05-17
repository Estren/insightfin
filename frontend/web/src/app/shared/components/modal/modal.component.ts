import { NgClass } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { AngularSvgIconModule } from 'angular-svg-icon';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
  imports: [AngularSvgIconModule, NgClass],
})
export class ModalComponent {
  title = input.required<string>();
  size = input<'md' | 'lg' | 'xl'>('md');
  dismissible = input<boolean>(true);
  close = output<void>();

  get widthClass(): string {
    return { md: 'max-w-md', lg: 'max-w-2xl', xl: 'max-w-4xl' }[this.size()];
  }

  requestClose(): void {
    if (this.dismissible()) this.close.emit();
  }
}
