import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'

vi.mock('@monaco-editor/react', () => ({
  default: ({ theme }: { theme: string }) => <output data-testid="monaco-theme">{theme}</output>,
  loader: { config: vi.fn() },
}))
vi.mock('monaco-editor', () => ({}))

import { EditorSettingsProvider } from '../../editor/EditorSettingsContext'
import CodeEditor from './CodeEditor'
import EditorSettings from './EditorSettings'

describe('CodeEditor theme', () => {
  afterEach(cleanup)

  it('updates Monaco when the saved color scheme changes', async () => {
    const user = userEvent.setup()
    render(
      <EditorSettingsProvider>
        <EditorSettings />
        <CodeEditor language="python" value="print(1)" />
      </EditorSettingsProvider>,
    )

    expect(screen.getByTestId('monaco-theme')).toHaveTextContent('vs-dark')
    await user.click(screen.getByRole('button', { name: 'Editor settings' }))
    await user.click(screen.getByRole('menuitem', { name: 'Light' }))
    expect(screen.getByTestId('monaco-theme')).toHaveTextContent('vs')
  })
})
