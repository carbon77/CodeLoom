# CodeLoom Frontend

React + TypeScript + Vite SPA with custom React controls and CSS Modules. Auth via Keycloak (OIDC authorization code + PKCE) using `oidc-client-ts`.

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
| `pnpm test`      | Vitest interaction and API tests         |
| `pnpm preview`   | Preview the production build             |

## Styling

- `src/styles/global.css` defines the reset, typography, and dark/light color tokens. CSS layers apply reset, base, shared components, then page styles.
- Shared controls and local SVG icons live in `src/components/ui/`. Component and page layouts use adjacent `.module.css` files; inline custom properties are limited to runtime editor dimensions.
- The navigation theme toggle defaults to dark and saves `codeloom.app.theme` in local storage. Monaco's four existing themes remain independent under `codeloom.editor.theme`.
- The solving workspace has resizable desktop panels (arrow keys, Home, and End also work) and stacks below 1024px. Tables and long code scroll within their own containers.

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
