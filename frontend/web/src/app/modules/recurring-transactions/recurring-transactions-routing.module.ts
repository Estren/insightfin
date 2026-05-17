import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RecurringFormComponent } from './pages/recurring-form/recurring-form.component';
import { RecurringListComponent } from './pages/recurring-list/recurring-list.component';

const routes: Routes = [
  {
    path: '',
    component: RecurringListComponent,
    children: [
      { path: 'new', component: RecurringFormComponent },
      { path: ':id/edit', component: RecurringFormComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RecurringTransactionsRoutingModule {}
