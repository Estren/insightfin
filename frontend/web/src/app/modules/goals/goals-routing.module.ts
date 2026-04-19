import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GoalContributeComponent } from './pages/goal-contribute/goal-contribute.component';
import { GoalFormComponent } from './pages/goal-form/goal-form.component';
import { GoalListComponent } from './pages/goal-list/goal-list.component';

const routes: Routes = [
  { path: '', component: GoalListComponent },
  { path: 'new', component: GoalFormComponent },
  { path: ':id/edit', component: GoalFormComponent },
  { path: ':id/contribute', component: GoalContributeComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class GoalsRoutingModule {}
