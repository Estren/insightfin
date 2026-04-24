import { MenuItem } from '../models/menu.model';

export class Menu {
  public static pages: MenuItem[] = [
    {
      group: 'Overview',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/chart-pie.svg',
          label: 'Dashboard',
          route: '/dashboard',
        },
      ],
    },
    {
      group: 'Finances',
      separator: true,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/view-grid.svg',
          label: 'Transactions',
          route: '/transactions',
        },
        {
          icon: 'assets/icons/heroicons/outline/bookmark.svg',
          label: 'Categories',
          route: '/categories',
        },
        {
          icon: 'assets/icons/heroicons/outline/shield-check.svg',
          label: 'Budgets',
          route: '/budgets',
        },
        {
          icon: 'assets/icons/heroicons/outline/arrow-sm-up.svg',
          label: 'Goals',
          route: '/goals',
        },
      ],
    },
  ];
}
