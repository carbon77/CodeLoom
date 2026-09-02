import {
  EventStreamContentType,
  fetchEventSource,
  type EventSourceMessage,
} from "@microsoft/fetch-event-source";
import { apiBaseUrl, apiFetch, authenticatedFetch } from "./client";

export type SubmissionLanguage = string;

export type SubmissionState =
  | "PENDING"
  | "COMPILING"
  | "COMPILE_ERROR"
  | "RUNNING"
  | "ACCEPTED"
  | "WRONG_ANSWER"
  | "RUNTIME_ERROR"
  | "TIME_LIMIT_EXCEEDED"
  | "MEMORY_LIMIT_EXCEEDED"
  | "SYSTEM_ERROR";

export interface Submission {
  submissionId: string;
  state: SubmissionState;
  language: SubmissionLanguage;
  createdAt: string;
}

export interface SubmissionStateResponse {
  submissionId: string;
  state: SubmissionState;
}

export interface TestCaseResult {
  input: string | null;
  expectedOutput: string | null;
  stdout: string | null;
  stderr: string | null;
  executionTimeMs: number | null;
  bytesUsed: number | null;
}

export interface SubmissionDetails extends Submission {
  code: string;
  errorMessage: string | null;
  results: TestCaseResult[];
}

export interface SendSubmissionPayload {
  problemId: number;
  code: string;
  language: SubmissionLanguage;
}

export function fetchSubmissions(problemId: number): Promise<Submission[]> {
  return apiFetch<Submission[]>(
    `/v1/submissions?problemId=${problemId}`,
  ).catch((error) => {
    console.error("Error fetching submissions:", error);
    throw error;
  });
}

export function fetchSubmissionDetails(
  submissionId: string,
): Promise<SubmissionDetails> {
  return apiFetch<SubmissionDetails>(`/v1/submissions/${submissionId}`).catch(
    (error) => {
      console.error("Error fetching submission details:", error);
      throw error;
    },
  );
}

export function sendSubmission(
  payload: SendSubmissionPayload,
): Promise<SubmissionStateResponse> {
  if (payload.code.trim() === "") {
    return Promise.reject(new Error("Code must not be blank."));
  }
  return apiFetch<SubmissionStateResponse>("/v1/submissions", {
    method: "POST",
    body: payload,
  }).catch((error) => {
    console.error("Error sending submission:", error);
    throw error;
  });
}

class FatalSseError extends Error {}

interface SubmissionStateSubscriptionOptions {
  signal: AbortSignal;
  onState: (state: SubmissionStateResponse) => void;
  onConnected?: () => void;
}

const states = new Set<SubmissionState>([
  "PENDING",
  "COMPILING",
  "COMPILE_ERROR",
  "RUNNING",
  "ACCEPTED",
  "WRONG_ANSWER",
  "RUNTIME_ERROR",
  "TIME_LIMIT_EXCEEDED",
  "MEMORY_LIMIT_EXCEEDED",
  "SYSTEM_ERROR",
]);

function parseStateMessage(
  message: EventSourceMessage,
): SubmissionStateResponse | null {
  if (message.event !== "submission-state") return null;
  try {
    const value = JSON.parse(message.data) as Partial<SubmissionStateResponse>;
    if (
      typeof value.submissionId !== "string" ||
      typeof value.state !== "string" ||
      !states.has(value.state as SubmissionState)
    ) {
      return null;
    }
    return value as SubmissionStateResponse;
  } catch {
    return null;
  }
}

export function subscribeToSubmissionStates({
  signal,
  onState,
  onConnected,
}: SubmissionStateSubscriptionOptions): Promise<void> {
  let retryAttempt = 0;

  return fetchEventSource(`${apiBaseUrl}/v1/submissions/sse`, {
    method: "GET",
    signal,
    fetch: authenticatedFetch,
    async onopen(response) {
      const contentType = response.headers.get("content-type");
      if (response.ok && contentType?.startsWith(EventStreamContentType)) {
        retryAttempt = 0;
        onConnected?.();
        return;
      }
      if (
        response.status >= 400 &&
        response.status < 500 &&
        response.status !== 429
      ) {
        throw new FatalSseError(
          `SSE request failed with status ${response.status}`,
        );
      }
      throw new Error(`SSE request failed with status ${response.status}`);
    },
    onmessage(message) {
      const state = parseStateMessage(message);
      if (state !== null) onState(state);
    },
    onclose() {
      throw new Error("Submission state stream closed");
    },
    onerror(error) {
      if (error instanceof FatalSseError) throw error;
      const delay = Math.min(30_000, 1_000 * 2 ** retryAttempt);
      retryAttempt += 1;
      return delay;
    },
  });
}
