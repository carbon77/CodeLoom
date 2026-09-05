import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as submissions from '../api/submissions'
import { fetchLanguageDisplay } from '../api/languages'
import { EditorSettingsProvider } from '../editor/EditorSettingsContext'
import { exampleProblem } from '../test/fixtures'
import ProblemDetailPage from './ProblemDetailPage'

vi.mock('@monaco-editor/react', () => ({
  default: ({ value, onChange, options }: { value: string; onChange?: (value: string) => void; options: { readOnly?: boolean } }) => <textarea aria-label="Source code" value={value} readOnly={options.readOnly} onChange={(event) => onChange?.(event.target.value)} />,
  loader: { config: vi.fn() },
}))
vi.mock('monaco-editor', () => ({}))
vi.mock('../api/languages', () => ({ fetchLanguageDisplay: vi.fn() }))
vi.mock('../api/submissions', () => ({ fetchSubmissions: vi.fn(), fetchSubmissionDetails: vi.fn(), sendSubmission: vi.fn(), subscribeToSubmissionStates: vi.fn() }))

describe('problem workspace', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(fetchLanguageDisplay).mockResolvedValue([{ key: 'python', name: 'Python' }, { key: 'java', name: 'Java' }])
    vi.mocked(submissions.fetchSubmissions).mockResolvedValue([{ submissionId: 'submission-1', language: 'python', state: 'RUNNING', createdAt: '2026-01-01' }])
    vi.mocked(submissions.sendSubmission).mockResolvedValue({ submissionId: 'submission-1', state: 'PENDING' })
    vi.mocked(submissions.subscribeToSubmissionStates).mockResolvedValue(undefined)
    vi.mocked(submissions.fetchSubmissionDetails).mockResolvedValue({ submissionId: 'submission-1', language: 'python', state: 'ACCEPTED', createdAt: '2026-01-01', code: 'print(3)', errorMessage: null, results: [{ input: '1 2', expectedOutput: '3', stdout: '3', stderr: '', executionTimeMs: 2, bytesUsed: 12 }] })
  })
  function setup() {
    const router = createMemoryRouter([{ id: 'problem', path: '/problems/:problemSlug', loader: () => exampleProblem, element: <ProblemDetailPage /> }], { initialEntries: ['/problems/two_sum'] })
    return render(<EditorSettingsProvider><RouterProvider router={router} /></EditorSettingsProvider>)
  }
  it('supports keyboard tabs and resizing without resetting source when language changes', async () => {
    const user = userEvent.setup()
    setup()
    await screen.findByRole('heading', { name: 'Two Sum' })
    const separator = screen.getByRole('separator', { name: 'Resize problem panel' })
    separator.focus()
    await user.keyboard('{ArrowRight}{End}')
    expect(separator).toHaveAttribute('aria-valuenow', '70')
    await user.keyboard('{Home}')
    expect(separator).toHaveAttribute('aria-valuenow', '20')
    const editor = screen.getByRole('textbox', { name: 'Source code' })
    await user.clear(editor)
    await user.type(editor, 'print(3)')
    await user.selectOptions(screen.getByRole('combobox', { name: 'Language' }), 'java')
    expect(editor).toHaveValue('print(3)')
    screen.getByRole('tab', { name: 'Problem', selected: true }).focus()
    await user.keyboard('{ArrowRight}')
    expect(await screen.findByRole('row', { name: /View submission/ })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /Submissions/, selected: true })).toHaveFocus()
  })
  it('submits source, follows live completion, and opens detailed results', async () => {
    const user = userEvent.setup()
    setup()
    const editor = await screen.findByRole('textbox', { name: 'Source code' })
    await waitFor(() => expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled())
    await user.clear(editor)
    await user.type(editor, 'print(3)')
    await user.click(screen.getByRole('button', { name: 'Submit' }))
    await screen.findByText('Submission sent successfully.')
    expect(submissions.sendSubmission).toHaveBeenCalledWith({ problemId: 7, code: 'print(3)', language: 'python' })
    const stream = vi.mocked(submissions.subscribeToSubmissionStates).mock.calls[0][0]
    act(() => stream.onState({ submissionId: 'submission-1', state: 'ACCEPTED' }))
    expect(await screen.findByText('Passed')).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Submission', selected: true })).toBeInTheDocument()
    await user.click(screen.getByText('Test case 1'))
    expect(screen.getByText('Standard output')).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Close submission details' }))
    expect(screen.getByRole('tab', { name: /Submissions/, selected: true })).toBeInTheDocument()
  })
})
