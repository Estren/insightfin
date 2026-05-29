import { MenuItem } from '../models/menu.model';

export class Menu {
  public static pages: MenuItem[] = [
    {
      group: 'nav.overview',
      separator: true,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/home.svg',
          label: 'nav.dashboard',
          route: '/dashboard',
        },
      ],
    },
    {
      group: 'nav.assistant',
      separator: true,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/chat-bubble.svg',
          label: 'nav.coach',
          route: '/coach',
        },
        {
          icon: 'assets/icons/heroicons/outline/information-circle.svg',
          label: 'nav.feedbacks',
          route: '/feedbacks',
        },
      ],
    },
    {
      group: 'nav.finances',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/view-grid.svg',
          label: 'nav.transactions',
          route: null,
          children: [
            {
              label: 'nav.transactionsList',
              route: '/transactions',
            },
            {
              label: 'nav.recurring',
              route: '/recurring',
            },
          ],
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
          icon: 'assets/icons/heroicons/outline/flag.svg',
          label: 'nav.goals',
          route: '/goals',
        },
        {
          icon: 'assets/icons/heroicons/outline/chart-pie.svg',
          label: 'nav.reports',
          route: '/reports',
        },
      ],
    },
  ];
}
