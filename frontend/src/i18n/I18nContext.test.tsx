import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { I18nProvider, useI18n } from './I18nContext'

function Consumer() {
  const { language, setLanguage, t } = useI18n()
  return <button onClick={() => setLanguage(language === 'en' ? 'ru' : 'en')}>{t('nav.problems')}</button>
}

describe('I18nProvider', () => {
  it('switches language and persists the selection', async () => {
    localStorage.setItem('codeloom.language', 'en')
    render(<I18nProvider><Consumer /></I18nProvider>)
    await userEvent.click(screen.getByRole('button', { name: 'Problems' }))
    expect(screen.getByRole('button', { name: 'Задачи' })).toBeInTheDocument()
    expect(localStorage.getItem('codeloom.language')).toBe('ru')
    expect(document.documentElement.lang).toBe('ru')
  })
})
