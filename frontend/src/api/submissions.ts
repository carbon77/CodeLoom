import { apiFetch } from "./client";

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
  id: string;
  userId: string;
  problemId: number;
  code: string;
  status: SubmissionStatus;
  language: string;
  createdAt: string;
}

export interface SendSubmissionPayload {
  problemId: number;
  code: string;
  language: string;
}

export function fetchSubmissions(problemId: number): Promise<Submission[]> {
  return apiFetch<Submission[]>(
    `/v1/submissions?problemId=${problemId}`,
  ).catch((error) => {
    console.error("Error fetching submissions:", error);
    throw error;
  });
}

export function sendSubmission(
  payload: SendSubmissionPayload,
): Promise<void> {
  return apiFetch<void>("/v1/submissions", {
    method: "POST",
    body: payload,
  }).catch((error) => {
    console.error("Error sending submission:", error);
    throw error;
  });
}
