import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BudgetFormComponent } from './pages/budget-form/budget-form.component';
import { BudgetListComponent } from './pages/budget-list/budget-list.component';

const routes: Routes = [
  {
    path: '',
    component: BudgetListComponent,
    children: [
      { path: 'new', component: BudgetFormComponent },
      { path: ':id/edit', component: BudgetFormComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BudgetsRoutingModule {}
