import { MenuItem } from '../models/menu.model';

export class Menu {
  public static pages: MenuItem[] = [
    {
      group: 'nav.overview',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/chart-pie.svg',
          label: 'nav.dashboard',
          route: '/dashboard',
        },
      ],
    },
    {
      group: 'nav.finances',
      separator: true,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/view-grid.svg',
          label: 'nav.transactions',
          route: '/transactions',
        },
        {
          icon: 'assets/icons/heroicons/outline/bookmark.svg',
          label: 'nav.categories',
          route: '/categories',
        },
        {
          icon: 'assets/icons/heroicons/outline/shield-check.svg',
          label: 'nav.budgets',
          route: '/budgets',
        },
        {
          icon: 'assets/icons/heroicons/outline/arrow-sm-up.svg',
          label: 'nav.goals',
          route: '/goals',
        },
      ],
    },
  ];
}
