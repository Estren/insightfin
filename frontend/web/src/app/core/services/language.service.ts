import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

const LANGUAGE_KEY = 'insightfin_language';
const DEFAULT_LANGUAGE = 'en-US';
const SUPPORTED_LANGUAGES = ['pt-BR', 'en-US'] as const;

export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number];

@Injectable({ providedIn: 'root' })
export class LanguageService {
  readonly supported: SupportedLanguage[] = [...SUPPORTED_LANGUAGES];

  constructor(private readonly translate: TranslateService) {}

  init(): void {
    this.translate.addLangs([...SUPPORTED_LANGUAGES]);
    this.translate.setDefaultLang(DEFAULT_LANGUAGE);
    const saved = localStorage.getItem(LANGUAGE_KEY) as SupportedLanguage | null;
    const lang = saved && SUPPORTED_LANGUAGES.includes(saved) ? saved : DEFAULT_LANGUAGE;
    this.translate.use(lang);
  }

  get current(): SupportedLanguage {
    return (this.translate.currentLang ?? DEFAULT_LANGUAGE) as SupportedLanguage;
  }

  setLanguage(lang: SupportedLanguage): void {
    this.translate.use(lang);
    localStorage.setItem(LANGUAGE_KEY, lang);
  }
}
