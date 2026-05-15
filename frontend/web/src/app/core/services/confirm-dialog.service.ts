import { Injectable, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export type ConfirmVariant = 'info' | 'warning' | 'danger';

export interface ConfirmDialogOptions {
  title: string;
  message: string;
  messageParams?: Record<string, string | number>;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: ConfirmVariant;
}

interface ConfirmDialogState extends ConfirmDialogOptions {
  subject: Subject<boolean>;
}

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  readonly state = signal<ConfirmDialogState | null>(null);

  confirm(options: ConfirmDialogOptions): Observable<boolean> {
    const subject = new Subject<boolean>();
    this.state.set({ ...options, subject });
    return subject.asObservable();
  }

  resolve(value: boolean): void {
    const current = this.state();
    if (!current) return;
    current.subject.next(value);
    current.subject.complete();
    this.state.set(null);
  }
}
