import { AsyncPipe, NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ConfirmDialogService } from '../../../../core/services/confirm-dialog.service';
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
    TranslateModule,
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
    { label: 'categories.all', value: 'ALL' },
    { label: 'categories.filterIncome', value: 'INCOME' },
    { label: 'categories.filterExpense', value: 'EXPENSE' },
  ];

  readonly categoryColors: string[] = [
    '#E11D48',
    '#6E56CF',
    '#CC0033',
    '#2490FF',
    '#EA580C',
    '#FACC15',
    '#22C55E',
  ];

  readonly emojis: string[] = [
    '🍔', '🍕', '🍜', '🍣', '🥗', '☕', '🍺', '🛒', '🥐', '🍱',
    '🚗', '🚌', '🚇', '✈️', '🚲', '⛽', '🛵', '🚕',
    '🏠', '💡', '🔧', '📦', '🧹', '🔑', '🛋️',
    '💊', '🏥', '💪', '🧘', '🦷', '👓',
    '🎬', '🎮', '🎵', '📚', '⚽', '🎭', '🎲', '🌴',
    '💰', '💳', '📈', '🏦', '💵', '💎',
    '👗', '👟', '🛍️', '💄', '👜',
    '🎓', '✏️', '💻', '📖',
    '🐾', '🎁', '📱', '🐶', '✂️', '🧴', '🌿', '🍼',
  ];

  form!: FormGroup;
  showForm = false;
  editingCategory: CategoryResponse | null = null;
  filterType: TransactionType | undefined;
  activeFilterTab = 'ALL';

  constructor(
    public readonly categoryStore: CategoryStore,
    private readonly fb: FormBuilder,
    private readonly confirmDialog: ConfirmDialogService,
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
      color: ['#E11D48'],
    });
  }

  openCreateForm(): void {
    this.editingCategory = null;
    this.form.reset({ name: '', type: 'EXPENSE', icon: '', color: '#E11D48' });
    this.showForm = true;
  }

  openEditForm(category: CategoryResponse): void {
    this.editingCategory = category;
    this.form.patchValue({
      name: category.name,
      type: category.type,
      icon: category.icon || '',
      color: category.color || '#E11D48',
    });
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingCategory = null;
  }

  selectColor(color: string): void {
    this.form.controls['color'].setValue(color);
  }

  selectEmoji(emoji: string): void {
    const current = this.form.controls['icon'].value;
    this.form.controls['icon'].setValue(current === emoji ? '' : emoji);
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
    this.confirmDialog
      .confirm({
        title: 'common.deleteTitle',
        message: 'common.deleteConfirm',
        messageParams: { name: category.name },
        confirmLabel: 'common.delete',
        variant: 'danger',
      })
      .subscribe((confirmed) => {
        if (confirmed) this.categoryStore.delete(category.id).subscribe();
      });
  }

  onFilterChange(tabValue: string): void {
    this.activeFilterTab = tabValue;
    const type = tabValue === 'ALL' ? undefined : (tabValue as TransactionType);
    this.filterType = type;
    this.categoryStore.load(type);
  }
}
