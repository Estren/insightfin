import { Component, input } from '@angular/core';
import { SkeletonComponent } from '../skeleton/skeleton.component';

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrl: './card.component.css',
  imports: [SkeletonComponent],
})
export class CardComponent {
  title = input<string>('');
  subtitle = input<string>('');
  /** When true, the card renders a skeleton in place of its title/subtitle and
   *  projected content — keeps wrapper, padding and rounding identical, so the
   *  skeleton can never drift from the real card. */
  loading = input<boolean>(false);
}
