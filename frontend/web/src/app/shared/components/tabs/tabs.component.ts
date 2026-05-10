import { Component, input, output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

export interface Tab {
  label: string;
  value: string;
}

@Component({
  selector: 'app-tabs',
  templateUrl: './tabs.component.html',
  styleUrl: './tabs.component.css',
  imports: [TranslateModule],
})
export class TabsComponent {
  tabs = input.required<Tab[]>();
  active = input.required<string>();
  tabChange = output<string>();
}
