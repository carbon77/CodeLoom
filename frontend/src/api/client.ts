import { userManager } from "../auth/keycloak";

export const apiBaseUrl =
  import.meta.env.VITE_API_URL ?? "http://localhost:8080";

async function accessToken(): Promise<string> {
  const user = await userManager.getUser();
  if (!user || user.expired) {
    throw new Error("No active session");
  }
  return user.access_token;
}

interface ApiFetchOptions {
  method?: string;
  body?: unknown;
}

export async function apiFetch<T>(
  path: string,
  options: ApiFetchOptions = {},
): Promise<T> {
  const token = await accessToken();
  const { method = "GET", body } = options;
  const response = await fetch(`${apiBaseUrl}${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  if (
    response.status === 204 ||
    response.headers.get("content-length") === "0"
  ) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
