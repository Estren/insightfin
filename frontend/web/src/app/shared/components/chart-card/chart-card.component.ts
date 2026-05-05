import { Component, input } from '@angular/core';

@Component({
  selector: 'app-chart-card',
  templateUrl: './chart-card.component.html',
})
export class ChartCardComponent {
  title = input.required<string>();
  subtitle = input<string>('');
}
