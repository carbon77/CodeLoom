import { userManager } from "../auth/keycloak";

export const apiBaseUrl =
  import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp?: string;
  path?: string;
  payload?: Record<string, unknown>;
}

export class ApiError extends Error {
  readonly status: number;
  readonly path?: string;
  readonly validation?: Record<string, unknown>;

  constructor(
    status: number,
    message: string,
    path?: string,
    validation?: Record<string, unknown>,
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.path = path;
    this.validation = validation;
  }
}

export function errorMessage(error: unknown, fallback: string): string {
  return error instanceof ApiError && error.message ? error.message : fallback;
}

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
    let error: ErrorResponse | undefined;
    try {
      error = (await response.json()) as ErrorResponse;
    } catch {
      // Empty and non-JSON error responses are valid fallbacks.
    }
    throw new ApiError(
      response.status,
      error?.message || `Request failed with status ${response.status}`,
      error?.path,
      error?.payload,
    );
  }

  if (
    response.status === 204 ||
    response.headers.get("content-length") === "0"
  ) {
    return undefined as T;
  }

  try {
    return (await response.json()) as T;
  } catch {
    throw new ApiError(response.status, "The server returned an invalid response.");
  }
}
