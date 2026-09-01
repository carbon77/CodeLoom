import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@monaco-editor/react', () => ({
  default: ({ value }: { value: string }) => <pre>{value}</pre>,
  loader: { config: vi.fn() },
}))
vi.mock('monaco-editor', () => ({}))

vi.mock('../../api/submissions', async (importOriginal) => {
  const original = await importOriginal<typeof import('../../api/submissions')>()
  return { ...original, fetchSubmissionDetails: vi.fn() }
})

import { fetchSubmissionDetails } from '../../api/submissions'
import { EditorSettingsProvider } from '../../editor/EditorSettingsContext'
import SubmissionDetails from './SubmissionDetails'

function renderSubmission() {
  return render(
    <EditorSettingsProvider>
      <SubmissionDetails submissionId="submission-1" refreshKey={0} />
    </EditorSettingsProvider>,
  )
}

describe('SubmissionDetails', () => {
  beforeEach(() => vi.mocked(fetchSubmissionDetails).mockReset())

  it('renders source, error, and test-case results', async () => {
    vi.mocked(fetchSubmissionDetails).mockResolvedValue({
      submissionId: 'submission-1',
      state: 'RUNTIME_ERROR',
      language: 'python',
      createdAt: '2026-01-01T10:00:00Z',
      code: 'print(1)',
      errorMessage: 'Execution failed',
      results: [{
        input: '1', expectedOutput: '2', stdout: '', stderr: 'boom',
        executionTimeMs: 12, bytesUsed: 1024,
      }],
    })

    renderSubmission()

    expect(await screen.findByText('Execution failed')).toBeInTheDocument()
    expect(screen.getByText('print(1)')).toBeInTheDocument()
    expect(screen.getByText('Test case 1')).toBeInTheDocument()
    expect(screen.getByText('Failed')).toBeInTheDocument()
    expect(screen.getByText('12 ms · 1024 bytes')).toBeInTheDocument()
    expect(screen.getByText('boom')).not.toBeVisible()
  })

  it('marks matching trimmed output as passed', async () => {
    vi.mocked(fetchSubmissionDetails).mockResolvedValue({
      submissionId: 'submission-1', state: 'ACCEPTED', language: 'python',
      createdAt: '2026-01-01T10:00:00Z', code: 'print(1)', errorMessage: null,
      results: [{ input: '', expectedOutput: '1', stdout: ' 1\n', stderr: '', executionTimeMs: 2, bytesUsed: 10 }],
    })

    renderSubmission()

    expect(await screen.findByText('Passed')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'Copy code' }).length).toBeGreaterThan(0)
  })

  it('shows a processing state when results are not ready', async () => {
    vi.mocked(fetchSubmissionDetails).mockResolvedValue({
      submissionId: 'submission-1', state: 'RUNNING', language: 'cpp',
      createdAt: '2026-01-01T10:00:00Z', code: 'int main() {}',
      errorMessage: null, results: [],
    })
    renderSubmission()
    expect(await screen.findByText('This submission is still being processed.')).toBeInTheDocument()
  })
})
