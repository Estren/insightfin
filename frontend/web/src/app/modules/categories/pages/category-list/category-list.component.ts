import { Component, OnInit } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryStore } from '../../../../core/stores/category.store';
import { CategoryResponse } from '../../../../core/models/category.model';
import { TransactionType } from '../../../../core/models/transaction.model';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  imports: [AsyncPipe, NgClass, ReactiveFormsModule],
})
export class CategoryListComponent implements OnInit {
  form!: FormGroup;
  showForm = false;
  editingCategory: CategoryResponse | null = null;
  filterType: TransactionType | undefined;

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
    this.categoryStore.delete(category.id).subscribe();
  }

  onFilterChange(type: TransactionType | undefined): void {
    this.filterType = type;
    this.categoryStore.load(type);
  }
}
