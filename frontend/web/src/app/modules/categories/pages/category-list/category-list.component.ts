import { AsyncPipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryResponse } from '../../../../core/models/category.model';
import { TransactionType } from '../../../../core/models/transaction.model';
import { CategoryStore } from '../../../../core/stores/category.store';
import { CardComponent } from '../../../../shared/components/card/card.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { Tab, TabsComponent } from '../../../../shared/components/tabs/tabs.component';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  imports: [
    AsyncPipe,
    NgClass,
    ReactiveFormsModule,
    CardComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingComponent,
    ModalComponent,
    TabsComponent,
  ],
})
export class CategoryListComponent implements OnInit {
  readonly filterTabs: Tab[] = [
    { label: 'All', value: 'ALL' },
    { label: 'Income', value: 'INCOME' },
    { label: 'Expense', value: 'EXPENSE' },
  ];

  form!: FormGroup;
  showForm = false;
  editingCategory: CategoryResponse | null = null;
  filterType: TransactionType | undefined;
  activeFilterTab = 'ALL';

  constructor(
    public readonly categoryStore: CategoryStore,
    private readonly fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.categoryStore.load();
    this.initForm();
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      type: ['EXPENSE', Validators.required],
      icon: [''],
      color: ['#6366f1'],
    });
  }

  openCreateForm(): void {
    this.editingCategory = null;
    this.form.reset({ name: '', type: 'EXPENSE', icon: '', color: '#6366f1' });
    this.showForm = true;
  }

  openEditForm(category: CategoryResponse): void {
    this.editingCategory = category;
    this.form.patchValue({
      name: category.name,
      type: category.type,
      icon: category.icon || '',
      color: category.color || '#6366f1',
    });
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingCategory = null;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const { name, type, icon, color } = this.form.value;
    const request = { name, type, icon: icon || undefined, color: color || undefined };

    if (this.editingCategory) {
      this.categoryStore.update(this.editingCategory.id, request).subscribe(() => {
        this.showForm = false;
        this.editingCategory = null;
      });
    } else {
      this.categoryStore.create(request).subscribe(() => {
        this.showForm = false;
      });
    }
  }

  onDelete(category: CategoryResponse): void {
    const confirmed = window.confirm(`Delete category "${category.name}"? This cannot be undone.`);
    if (!confirmed) return;
    this.categoryStore.delete(category.id).subscribe();
  }

  onFilterChange(tabValue: string): void {
    this.activeFilterTab = tabValue;
    const type = tabValue === 'ALL' ? undefined : (tabValue as TransactionType);
    this.filterType = type;
    this.categoryStore.load(type);
  }
}
