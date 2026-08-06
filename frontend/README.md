# CodeLoom Frontend

React + TypeScript + Vite SPA styled with MUI. Auth via Keycloak (OIDC authorization code + PKCE) using `oidc-client-ts`.

## Getting started

```bash
pnpm install
pnpm dev   # http://localhost:5173
```

Copy `.env.example` to `.env` and adjust if your Keycloak or app URL differs. `.env` is gitignored.

## Scripts

| Command          | Description                              |
| ---------------- | ---------------------------------------- |
| `pnpm dev`       | Vite dev server                          |
| `pnpm type-check`| `tsc -b` (no emit)                       |
| `pnpm build`     | Type-check + production build            |
| `pnpm lint`      | Oxlint                                   |
| `pnpm preview`   | Preview the production build             |

## Auth flow

- `/` is guarded by `RequireAuth` (`src/components/RequireAuth.tsx`): unauthenticated visitors are redirected to Keycloak via `userManager.signinRedirect()`.
- Keycloak redirects back to `/callback`, which completes the flow (`signinRedirectCallback`) and restores the original path.
- `src/pages/ProfilePage.tsx` renders the profile from ID-token claims and has a **Log out** button that calls `userManager.signoutRedirect()`. Keycloak ends the SSO session and returns to `/logout`, which clears the local session.
- Tokens are kept in `localStorage` and silently renewed (`automaticSilentRenew`).
- Auth config lives in `src/auth/keycloak.ts`, read from `VITE_*` env vars.

## Keycloak client setup

The app expects a **public** client in the realm `codeloom` (defaults come from `.env`):

- Client ID: `codeloom-frontend`
- Client authentication: off (public)
- Valid redirect URIs: `http://localhost:5173/*`
- Valid post-logout redirect URIs: `http://localhost:5173/*`
- Web origins: `http://localhost:5173`
- Standard flow enabled (PKCE auto-applied)

Adjust the URIs to match `VITE_APP_URL`.
