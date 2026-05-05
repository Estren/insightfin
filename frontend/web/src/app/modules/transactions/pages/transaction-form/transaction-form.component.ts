import { AsyncPipe, NgClass } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ControlContainer, FormGroup, FormGroupDirective, ReactiveFormsModule } from '@angular/forms';
import { combineLatest, map, Observable, startWith } from 'rxjs';
import { CategoryResponse } from '../../../../core/models/category.model';
import { TransactionType } from '../../../../core/models/transaction.model';
import { CategoryStore } from '../../../../core/stores/category.store';

@Component({
  selector: 'app-transaction-form',
  templateUrl: './transaction-form.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: FormGroupDirective }],
  imports: [AsyncPipe, NgClass, ReactiveFormsModule],
})
export class TransactionFormComponent implements OnInit {
  filteredCategories$!: Observable<CategoryResponse[]>;

  private readonly categoryStore = inject(CategoryStore);
  private readonly fgd = inject(FormGroupDirective);

  get form(): FormGroup {
    return this.fgd.form;
  }

  ngOnInit(): void {
    this.categoryStore.load();
    const typeControl = this.form.controls['type'];
    this.filteredCategories$ = combineLatest([
      this.categoryStore.categories$,
      typeControl.valueChanges.pipe(startWith(typeControl.value), map((v) => v as TransactionType)),
    ]).pipe(map(([categories, type]) => categories.filter((c) => c.type === type)));
  }
}
