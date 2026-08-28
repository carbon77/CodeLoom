import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import SubmissionsList from './SubmissionsList'

const submission = {
  submissionId: 'submission-1',
  status: 'ACCEPTED' as const,
  language: 'python' as const,
  createdAt: '2026-01-01T10:00:00Z',
}

describe('SubmissionsList', () => {
  it('opens a submission when its row is selected', async () => {
    const onSelectSubmission = vi.fn()
    render(
      <SubmissionsList
        submissions={[submission]}
        error={null}
        onSelectSubmission={onSelectSubmission}
      />,
    )
    await userEvent.click(screen.getByRole('row', { name: /view submission submission-1/i }))
    expect(onSelectSubmission).toHaveBeenCalledWith('submission-1')
  })
})
