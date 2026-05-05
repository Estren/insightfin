import { Component, input, output } from '@angular/core';
import { AngularSvgIconModule } from 'angular-svg-icon';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
  imports: [AngularSvgIconModule],
})
export class ModalComponent {
  title = input.required<string>();
  close = output<void>();
}
