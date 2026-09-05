import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  EditorSettingsProvider,
  editorThemeStorageKey,
  useEditorSettings,
} from '../../editor/EditorSettingsContext'
import EditorSettings from './EditorSettings'

function CurrentTheme() {
  const { theme } = useEditorSettings()
  return <output>{theme}</output>
}

function renderSettings() {
  return render(
    <EditorSettingsProvider>
      <EditorSettings />
      <CurrentTheme />
    </EditorSettingsProvider>,
  )
}

describe('EditorSettings', () => {
  let values: Map<string, string>

  beforeEach(() => {
    values = new Map()
    const storage: Storage = {
      get length() { return values.size },
      clear: () => values.clear(),
      getItem: (key) => values.get(key) ?? null,
      key: (index) => [...values.keys()][index] ?? null,
      removeItem: (key) => { values.delete(key) },
      setItem: (key, value) => { values.set(key, value) },
    }
    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      value: storage,
    })
  })
  afterEach(cleanup)

  it('uses the dark editor theme by default', () => {
    renderSettings()
    expect(screen.getByText('vs-dark')).toBeInTheDocument()
  })

  it('restores a valid saved theme and ignores an invalid one', () => {
    window.localStorage.setItem(editorThemeStorageKey, 'hc-light')
    const view = renderSettings()
    expect(screen.getByText('hc-light')).toBeInTheDocument()

    view.unmount()
    window.localStorage.setItem(editorThemeStorageKey, 'invalid')
    renderSettings()
    expect(screen.getByText('vs-dark')).toBeInTheDocument()
  })

  it('offers all schemes and persists the selected theme', async () => {
    const user = userEvent.setup()
    renderSettings()

    await user.click(screen.getByRole('button', { name: 'Editor settings' }))
    expect(screen.getByRole('menu', { name: 'Editor color scheme' })).toBeInTheDocument()
    expect(screen.getAllByRole('menuitemradio')).toHaveLength(4)

    await user.click(screen.getByRole('menuitemradio', { name: /high contrast dark/i }))
    expect(screen.getByText('hc-black')).toBeInTheDocument()
    expect(window.localStorage.getItem(editorThemeStorageKey)).toBe('hc-black')
  })

  it('keeps working when local storage is unavailable', async () => {
    vi.spyOn(window.localStorage, 'getItem').mockImplementation(() => { throw new Error('blocked') })
    vi.spyOn(window.localStorage, 'setItem').mockImplementation(() => { throw new Error('blocked') })
    const user = userEvent.setup()
    renderSettings()

    expect(screen.getByText('vs-dark')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Editor settings' }))
    await user.click(screen.getByRole('menuitemradio', { name: /^light$/i }))
    expect(screen.getByText('vs')).toBeInTheDocument()
  })
})
