import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppThemeProvider, appThemeStorageKey, useAppTheme } from './AppTheme'

function Toggle() {
  const { theme, setTheme } = useAppTheme()
  return <button onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>{theme}</button>
}
describe('app theme', () => {
  let values: Map<string, string>
  beforeEach(() => {
    values = new Map()
    Object.defineProperty(window, 'localStorage', {
      configurable: true, value: {
        getItem: vi.fn((key: string) => values.get(key) ?? null),
        setItem: vi.fn((key: string, value: string) => values.set(key, value)),
      }
    })
  })
  it('defaults to dark, persists the selected theme, and restores it on remount', async () => {
    const user = userEvent.setup()
    const view = render(<AppThemeProvider><Toggle /></AppThemeProvider>)
    expect(document.documentElement).toHaveAttribute('data-theme', 'dark')
    await user.click(screen.getByRole('button', { name: 'dark' }))
    expect(document.documentElement).toHaveAttribute('data-theme', 'light')
    expect(values.get(appThemeStorageKey)).toBe('light')
    view.unmount()
    render(<AppThemeProvider><Toggle /></AppThemeProvider>)
    expect(screen.getByRole('button', { name: 'light' })).toBeInTheDocument()
  })
  it('ignores invalid preferences and keeps working when storage is blocked', async () => {
    values.set(appThemeStorageKey, 'invalid')
    const view = render(<AppThemeProvider><Toggle /></AppThemeProvider>)
    expect(screen.getByRole('button', { name: 'dark' })).toBeInTheDocument()
    view.unmount()
    vi.mocked(localStorage.getItem).mockImplementation(() => { throw new Error('blocked') })
    vi.mocked(localStorage.setItem).mockImplementation(() => { throw new Error('blocked') })
    render(<AppThemeProvider><Toggle /></AppThemeProvider>)
    await userEvent.click(screen.getByRole('button', { name: 'dark' }))
    expect(document.documentElement).toHaveAttribute('data-theme', 'light')
  })
})
