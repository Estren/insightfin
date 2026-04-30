import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout.component';

const routes: Routes = [
  {
    path: 'dashboard',
    component: LayoutComponent,
    loadChildren: () => import('../dashboard/dashboard.module').then((m) => m.DashboardModule),
  },
  {
    path: 'categories',
    component: LayoutComponent,
    loadChildren: () => import('../categories/categories.module').then((m) => m.CategoriesModule),
  },
  {
    path: 'transactions',
    component: LayoutComponent,
    loadChildren: () => import('../transactions/transactions.module').then((m) => m.TransactionsModule),
  },
  {
    path: 'budgets',
    component: LayoutComponent,
    loadChildren: () => import('../budgets/budgets.module').then((m) => m.BudgetsModule),
  },
  {
    path: 'goals',
    component: LayoutComponent,
    loadChildren: () => import('../goals/goals.module').then((m) => m.GoalsModule),
  },
  {
    path: 'profile',
    component: LayoutComponent,
    loadChildren: () => import('../profile/profile.module').then((m) => m.ProfileModule),
  },
  {
    path: 'components',
    component: LayoutComponent,
    loadChildren: () => import('../uikit/uikit.module').then((m) => m.UikitModule),
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'error/404' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LayoutRoutingModule {}
