import { UserManager, WebStorageStateStore } from "oidc-client-ts";

export const appUrl = import.meta.env.VITE_APP_URL ?? "http://localhost:5173";
export const keycloakUrl =
  import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8080";
export const keycloakRealm = import.meta.env.VITE_KEYCLOAK_REALM ?? "codeloom";
export const keycloakClientId =
  import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "codeloom-frontend";

export const userManager = new UserManager({
  authority: `${keycloakUrl}/realms/${keycloakRealm}`,
  client_id: keycloakClientId,
  redirect_uri: `${appUrl}/callback`,
  post_logout_redirect_uri: `${appUrl}/logout`,
  response_type: "code",
  scope: "openid profile email",
  automaticSilentRenew: true,
  userStore: new WebStorageStateStore({ store: window.localStorage }),
});

export async function signIn(): Promise<void> {
  await userManager.signinRedirect();
}

let signInCallbackPromise: Promise<string | undefined> | null = null;

export function handleSignInRedirect(): Promise<string | undefined> {
  signInCallbackPromise ??= userManager
    .signinRedirectCallback()
    .then((user) => (typeof user.state === "string" ? user.state : undefined))
    .finally(() => {
      signInCallbackPromise = null;
    });
  return signInCallbackPromise;
}

export async function signOut(): Promise<void> {
  await userManager.signoutRedirect();
}

export async function completeSignOut(): Promise<void> {
  await userManager.removeUser();
}
