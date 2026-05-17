import { Component, inject, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ImageCropperComponent, ImageCroppedEvent, ImageTransform } from 'ngx-image-cropper';
import { ToastService } from '../../../core/services/toast.service';
import { ModalComponent } from '../modal/modal.component';

@Component({
  selector: 'app-avatar-cropper-modal',
  templateUrl: './avatar-cropper-modal.component.html',
  imports: [FormsModule, TranslateModule, ImageCropperComponent, ModalComponent],
})
export class AvatarCropperModalComponent {
  private readonly toastService = inject(ToastService);

  file = input.required<File>();
  uploading = input<boolean>(false);
  confirm = output<File>();
  cancel = output<void>();

  zoom = 1;
  transform: ImageTransform = { scale: 1 };

  private currentBlob: Blob | null = null;

  onCropped(event: ImageCroppedEvent): void {
    this.currentBlob = event.blob ?? null;
  }

  onZoomChange(value: number): void {
    this.zoom = value;
    this.transform = { ...this.transform, scale: value };
  }

  onConfirm(): void {
    if (!this.currentBlob) return;
    const cropped = new File([this.currentBlob], 'avatar.jpg', { type: 'image/jpeg' });
    this.confirm.emit(cropped);
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onLoadFailed(): void {
    this.toastService.error('avatarCropper.errors.loadFailed');
    this.cancel.emit();
  }
}
