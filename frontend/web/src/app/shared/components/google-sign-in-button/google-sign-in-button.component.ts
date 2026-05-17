import { AfterViewInit, Component, ElementRef, NgZone, ViewChild, output } from '@angular/core';
import { environment } from '../../../../environments/environment';

declare const google: {
  accounts: {
    id: {
      initialize: (config: {
        client_id: string;
        nonce: string;
        callback: (response: { credential: string }) => void;
      }) => void;
      renderButton: (element: HTMLElement, options: Record<string, unknown>) => void;
    };
  };
};

export interface GoogleCredentialEvent {
  credential: string;
  nonce: string;
}

@Component({
  selector: 'app-google-sign-in-button',
  template: '<div #target class="flex justify-center"></div>',
})
export class GoogleSignInButtonComponent implements AfterViewInit {
  @ViewChild('target', { static: true }) target!: ElementRef<HTMLDivElement>;

  readonly credential = output<GoogleCredentialEvent>();

  private nonce = '';

  constructor(private readonly zone: NgZone) {}

  ngAfterViewInit(): void {
    this.initialize(0);
  }

  private initialize(retries: number): void {
    if (!environment.googleClientId) return;
    if (typeof google === 'undefined' || !google.accounts?.id) {
      if (retries < 20) {
        setTimeout(() => this.initialize(retries + 1), 200);
      }
      return;
    }
    this.nonce = crypto.randomUUID();
    google.accounts.id.initialize({
      client_id: environment.googleClientId,
      nonce: this.nonce,
      callback: (response) =>
        this.zone.run(() => this.credential.emit({ credential: response.credential, nonce: this.nonce })),
    });
    google.accounts.id.renderButton(this.target.nativeElement, {
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      width: 320,
    });
  }
}
