// oxlint-disable react/only-export-components -- The context exposes its provider and hook together.
import { createContext, useContext, useLayoutEffect, useState, type ReactNode } from 'react'

export type AppTheme = 'dark' | 'light'
export const appThemeStorageKey = 'codeloom.app.theme'
function storedTheme(): AppTheme {
  try { return localStorage.getItem(appThemeStorageKey) === 'light' ? 'light' : 'dark' } catch { return 'dark' }
}
const ThemeContext = createContext<{ theme: AppTheme; setTheme: (theme: AppTheme) => void } | null>(null)
export function AppThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<AppTheme>(storedTheme)
  useLayoutEffect(() => {
    document.documentElement.dataset.theme = theme
    try { localStorage.setItem(appThemeStorageKey, theme) } catch { /* Keep the in-memory preference. */ }
  }, [theme])
  return <ThemeContext.Provider value={{ theme, setTheme }}>{children}</ThemeContext.Provider>
}
export function useAppTheme() {
  const theme = useContext(ThemeContext)
  if (!theme) throw new Error('useAppTheme requires AppThemeProvider')
  return theme
}
