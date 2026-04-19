import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TransactionFormComponent } from './pages/transaction-form/transaction-form.component';
import { TransactionListComponent } from './pages/transaction-list/transaction-list.component';

const routes: Routes = [
  { path: '', component: TransactionListComponent },
  { path: 'new', component: TransactionFormComponent },
  { path: ':id/edit', component: TransactionFormComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TransactionsRoutingModule {}
