import { describe, expect, it } from 'vitest'
import type { User } from 'oidc-client-ts'
import { getRoles, isAdmin } from './roles'

const user = (profile: Record<string, unknown>) => ({ profile } as unknown as User)

describe('roles', () => {
  it('reads Keycloak realm roles', () => expect(isAdmin(user({ realm_access: { roles: ['ROLE_ADMIN'] } }))).toBe(true))
  it('retains the flat roles fallback', () => expect(getRoles(user({ roles: ['ROLE_USER'] }))).toEqual(['ROLE_USER']))
})
