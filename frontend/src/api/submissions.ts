import {
  EventStreamContentType,
  fetchEventSource,
  type EventSourceMessage,
} from "@microsoft/fetch-event-source";
import { apiBaseUrl, apiFetch, authenticatedFetch } from "./client";

export type SubmissionLanguage = "java" | "cpp" | "python";

export type SubmissionStatus =
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
  status: SubmissionStatus;
  language: SubmissionLanguage;
  createdAt: string;
}

export interface SubmissionStatusResponse {
  submissionId: string;
  status: SubmissionStatus;
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
): Promise<SubmissionStatusResponse> {
  if (payload.code.trim() === "") {
    return Promise.reject(new Error("Code must not be blank."));
  }
  return apiFetch<SubmissionStatusResponse>("/v1/submissions", {
    method: "POST",
    body: payload,
  }).catch((error) => {
    console.error("Error sending submission:", error);
    throw error;
  });
}

class FatalSseError extends Error {}

interface SubmissionStatusSubscriptionOptions {
  signal: AbortSignal;
  onStatus: (status: SubmissionStatusResponse) => void;
  onConnected?: () => void;
}

const statuses = new Set<SubmissionStatus>([
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

function parseStatusMessage(
  message: EventSourceMessage,
): SubmissionStatusResponse | null {
  if (message.event !== "submission-status") return null;
  try {
    const value = JSON.parse(message.data) as Partial<SubmissionStatusResponse>;
    if (
      typeof value.submissionId !== "string" ||
      typeof value.status !== "string" ||
      !statuses.has(value.status as SubmissionStatus)
    ) {
      return null;
    }
    return value as SubmissionStatusResponse;
  } catch {
    return null;
  }
}

export function subscribeToSubmissionStatuses({
  signal,
  onStatus,
  onConnected,
}: SubmissionStatusSubscriptionOptions): Promise<void> {
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
      const status = parseStatusMessage(message);
      if (status !== null) onStatus(status);
    },
    onclose() {
      throw new Error("Submission status stream closed");
    },
    onerror(error) {
      if (error instanceof FatalSseError) throw error;
      const delay = Math.min(30_000, 1_000 * 2 ** retryAttempt);
      retryAttempt += 1;
      return delay;
    },
  });
}
