import { describe, expect, it, vi } from 'vitest'
vi.mock('./client', () => ({ apiFetch: vi.fn() }))
import { apiFetch } from './client'
import { sendSubmission } from './submissions'

describe('submission contracts', () => {
  it('rejects blank code without a request', async () => {
    await expect(sendSubmission({ problemId: 1, code: '  ', language: 'python' })).rejects.toThrow('Code must not be blank')
    expect(apiFetch).not.toHaveBeenCalled()
  })
})
