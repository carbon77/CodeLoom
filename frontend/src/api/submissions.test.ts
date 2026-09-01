import { beforeEach, describe, expect, it, vi } from 'vitest'

const { fetchEventSource } = vi.hoisted(() => ({ fetchEventSource: vi.fn() }))
vi.mock('@microsoft/fetch-event-source', () => ({
  EventStreamContentType: 'text/event-stream',
  fetchEventSource,
}))
vi.mock('./client', () => ({
  apiBaseUrl: 'http://api.test',
  apiFetch: vi.fn(),
  authenticatedFetch: vi.fn(),
}))
import { apiFetch, authenticatedFetch } from './client'
import {
  fetchSubmissionDetails,
  sendSubmission,
  subscribeToSubmissionStates,
} from './submissions'

describe('submission contracts', () => {
  beforeEach(() => {
    vi.mocked(apiFetch).mockReset()
    fetchEventSource.mockReset().mockResolvedValue(undefined)
  })

  it('rejects blank code without a request', async () => {
    await expect(sendSubmission({ problemId: 1, code: '  ', language: 'python' })).rejects.toThrow('Code must not be blank')
    expect(apiFetch).not.toHaveBeenCalled()
  })

  it('fetches submission details by id', async () => {
    vi.mocked(apiFetch).mockResolvedValue({ submissionId: 'submission-1' })
    await fetchSubmissionDetails('submission-1')
    expect(apiFetch).toHaveBeenCalledWith('/v1/submissions/submission-1')
  })

  it('subscribes with authenticated fetch and emits valid state events', async () => {
    const onState = vi.fn()
    const onConnected = vi.fn()
    const controller = new AbortController()

    await subscribeToSubmissionStates({ signal: controller.signal, onState, onConnected })
    expect(fetchEventSource).toHaveBeenCalledWith(
      'http://api.test/v1/submissions/sse',
      expect.objectContaining({
        method: 'GET',
        signal: controller.signal,
        fetch: authenticatedFetch,
      }),
    )

    const options = fetchEventSource.mock.calls[0][1]
    await options.onopen(new Response(null, {
      status: 200,
      headers: { 'content-type': 'text/event-stream;charset=UTF-8' },
    }))
    options.onmessage({ event: 'submission-state', data: JSON.stringify({
      submissionId: 'submission-1', state: 'ACCEPTED',
    }), id: '', retry: '' })
    options.onmessage({ event: 'other', data: '{}', id: '', retry: '' })
    options.onmessage({ event: 'submission-state', data: 'invalid', id: '', retry: '' })

    expect(onConnected).toHaveBeenCalledOnce()
    expect(onState).toHaveBeenCalledOnce()
    expect(onState).toHaveBeenCalledWith({ submissionId: 'submission-1', state: 'ACCEPTED' })
  })

  it('stops retrying on fatal client responses', async () => {
    const controller = new AbortController()
    await subscribeToSubmissionStates({ signal: controller.signal, onState: vi.fn() })
    const options = fetchEventSource.mock.calls[0][1]
    const error = await options.onopen(new Response(null, { status: 401 })).catch((cause: unknown) => cause)
    expect(() => options.onerror(error)).toThrow('SSE request failed with status 401')
  })
})
