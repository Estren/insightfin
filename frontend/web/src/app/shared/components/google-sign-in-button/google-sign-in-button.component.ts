import { AfterViewInit, Component, ElementRef, NgZone, ViewChild, effect, output } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { ThemeService } from '../../../core/services/theme.service';

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
  private ready = false;

  constructor(
    private readonly zone: NgZone,
    private readonly theme: ThemeService,
  ) {
    // Google's GIS button has no live theme support, so re-render it whenever
    // the app theme flips — otherwise the light "outline" button stays bright
    // on a dark background.
    effect(() => {
      this.theme.theme();
      if (this.ready) this.renderButton();
    });
  }

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
    this.ready = true;
    this.renderButton();
  }

  private renderButton(): void {
    if (!this.ready) return;
    // Clear first so a theme toggle re-renders instead of stacking buttons.
    this.target.nativeElement.innerHTML = '';
    google.accounts.id.renderButton(this.target.nativeElement, {
      theme: this.theme.isDark ? 'filled_black' : 'outline',
      size: 'large',
      text: 'continue_with',
      width: 320,
    });
  }
}
