import { Component, input } from '@angular/core';

@Component({
  selector: 'app-item-row',
  templateUrl: './item-row.component.html',
  styleUrl: './item-row.component.css',
})
export class ItemRowComponent {
  title = input.required<string>();
  subtitle = input<string>('');
  value = input<string>('');
  color = input<string>('');
  positive = input<boolean>(false);
  negative = input<boolean>(false);
}
