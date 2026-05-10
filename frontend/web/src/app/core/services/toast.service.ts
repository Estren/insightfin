import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { toast } from 'ngx-sonner';

@Injectable({ providedIn: 'root' })
export class ToastService {
  constructor(private readonly translate: TranslateService) {}

  success(key: string): void {
    toast.success(this.translate.instant(key));
  }

  error(key: string): void {
    toast.error(this.translate.instant(key));
  }

  info(key: string): void {
    toast.info(this.translate.instant(key));
  }
}
