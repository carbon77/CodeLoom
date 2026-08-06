import { useSyncExternalStore } from 'react'
import type { User } from 'oidc-client-ts'
import { userManager } from './keycloak'

type AuthState = User | null | undefined

let currentUser: AuthState = undefined
const listeners = new Set<() => void>()

function emit(): void {
  listeners.forEach((listener) => listener())
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

const getSnapshot = (): AuthState => currentUser

userManager.events.addUserLoaded((user) => {
  currentUser = user
  emit()
})

userManager.events.addUserUnloaded(() => {
  currentUser = null
  emit()
})

void userManager.getUser().then((user) => {
  currentUser = user
  emit()
})

export function useAuth(): AuthState {
  return useSyncExternalStore(subscribe, getSnapshot)
}
