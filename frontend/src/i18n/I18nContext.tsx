import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import { translations, type TranslationKey } from './translations'

export type Language = 'en' | 'ru'

const STORAGE_KEY = 'codeloom.language'

function initialLanguage(): Language {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'en' || saved === 'ru') return saved
  return navigator.language.toLowerCase().startsWith('ru') ? 'ru' : 'en'
}

interface I18nValue {
  language: Language
  locale: string
  setLanguage: (language: Language) => void
  t: (key: TranslationKey, values?: Record<string, string | number>) => string
}

function translateEnglish(key: TranslationKey, values?: Record<string, string | number>): string {
  let result: string = translations.en[key] ?? key
  for (const [name, value] of Object.entries(values ?? {})) result = result.replaceAll(`{{${name}}}`, String(value))
  return result
}

const I18nContext = createContext<I18nValue>({
  language: 'en', locale: 'en-US', setLanguage: () => undefined, t: translateEnglish,
})

export function I18nProvider({ children }: { children: ReactNode }) {
  const [language, updateLanguage] = useState<Language>(initialLanguage)

  const setLanguage = useCallback((next: Language) => {
    localStorage.setItem(STORAGE_KEY, next)
    document.documentElement.lang = next
    updateLanguage(next)
  }, [])

  const value = useMemo<I18nValue>(() => ({
    language,
    locale: language === 'ru' ? 'ru-RU' : 'en-US',
    setLanguage,
    t: (key, values) => {
      let result: string = translations[language][key] ?? translations.en[key] ?? key
      for (const [name, value] of Object.entries(values ?? {})) {
        result = result.replaceAll(`{{${name}}}`, String(value))
      }
      return result
    },
  }), [language, setLanguage])

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}

export function useI18n(): I18nValue {
  return useContext(I18nContext)
}
