import { Component, input, output } from '@angular/core';

export interface Tab {
  label: string;
  value: string;
}

@Component({
  selector: 'app-tabs',
  templateUrl: './tabs.component.html',
  styleUrl: './tabs.component.css',
})
export class TabsComponent {
  tabs = input.required<Tab[]>();
  active = input.required<string>();
  tabChange = output<string>();
}
