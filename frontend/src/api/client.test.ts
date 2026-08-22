import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../auth/keycloak', () => ({
  userManager: { getUser: vi.fn().mockResolvedValue({ expired: false, access_token: 'token' }) },
}))

import { ApiError, apiFetch } from './client'

describe('apiFetch', () => {
  beforeEach(() => vi.stubGlobal('fetch', vi.fn()))

  it('returns JSON and supports empty success responses', async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce(new Response(JSON.stringify({ id: 1 }), { status: 200, headers: { 'content-type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(null, { status: 204 }))
    await expect(apiFetch('/json')).resolves.toEqual({ id: 1 })
    await expect(apiFetch('/empty')).resolves.toBeUndefined()
  })

  it('preserves structured backend errors', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify({
      status: 400, message: 'Title must not be blank', path: '/v1/problems/1', payload: { title: 'required' },
    }), { status: 400, headers: { 'content-type': 'application/json' } }))
    const error = await apiFetch('/failure').catch((cause: unknown) => cause)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 400, message: 'Title must not be blank', path: '/v1/problems/1', validation: { title: 'required' } })
  })

  it('falls back safely for non-JSON errors', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response('oops', { status: 502 }))
    await expect(apiFetch('/failure')).rejects.toMatchObject({ status: 502, message: 'Request failed with status 502' })
  })
})
