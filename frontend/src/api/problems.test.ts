import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('./client', () => ({ apiFetch: vi.fn() }))
import { apiFetch } from './client'
import { fetchTestCases, updateProblem, updateTestCase } from './problems'

describe('problem API contracts', () => {
  beforeEach(() => vi.mocked(apiFetch).mockResolvedValue(undefined))

  it('sends the full problem payload including topics', async () => {
    const payload = { title: 'Two Sum', slug: 'two_sum', description: '', difficulty: 'EASY' as const,
      constraints: { executionTimeLimitMs: null, memoryUsageLimitBytes: null }, examples: { examples: [] }, hints: [],
      topics: [{ topic_id: 'topic-1' }, { name: 'Graphs' }] }
    await updateProblem(7, payload)
    expect(apiFetch).toHaveBeenCalledWith('/v1/problems/7', { method: 'PUT', body: payload })
  })

  it('omits visibility unless requested and updates test cases with PUT', async () => {
    await fetchTestCases(7)
    expect(apiFetch).toHaveBeenCalledWith('/v1/testCases/by-problem-id/7')
    const testCase = { problemId: 7, input: '1', expectedOutput: '2', isPublic: true }
    await updateTestCase('case-1', testCase)
    expect(apiFetch).toHaveBeenCalledWith('/v1/testCases/case-1', { method: 'PUT', body: testCase })
  })
})
