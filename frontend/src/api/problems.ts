import { apiFetch } from "./client";

export type Difficulty = "EASY" | "MEDIUM" | "HARD";

export interface ProblemListDto {
  problemId: number;
  title: string;
  slug: string;
  difficulty: Difficulty;
  publishedAt: string | null;
}

export interface ProblemConstraints {
  executionTimeLimitMs?: number | null;
  memoryUsageLimitBytes?: number | null;
}

export interface ProblemExample {
  input: string;
  output: string;
  explanation?: string | null;
}

export interface Problem {
  id: number;
  slug: string;
  title: string;
  description: string;
  difficulty: Difficulty;
  constraints: ProblemConstraints | null;
  examples: { examples: ProblemExample[] } | null;
  hints: string[];
}

export interface ProblemDto extends Problem {
  testCases: TestCaseResponse[];
  topics: Topic[];
}

export type ProblemDetail = ProblemDto;

export interface TestCaseRequest {
  problemId: number;
  input: string;
  expectedOutput: string;
  isPublic: boolean;
}

export interface TestCaseResponse extends TestCaseRequest { id: string }
export type TestCase = Omit<TestCaseRequest, "problemId"> & { id?: string; problemId?: number };

export type ProblemTopicRequest = { topic_id: string } | { name: string };

export interface ProblemUpdatePayload {
  title: string;
  slug: string;
  description: string;
  difficulty: Difficulty;
  constraints: ProblemConstraints | null;
  examples: { examples: ProblemExample[] } | null;
  hints: string[];
  topics: ProblemTopicRequest[];
}

export interface Topic {
  id: string;
  name: string;
}

export function fetchProblems(
  params?: Record<string, string | string[]>,
): Promise<ProblemListDto[]> {
  const searchParams = new URLSearchParams();
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (Array.isArray(value)) {
        value.forEach((item) => searchParams.append(key, item));
      } else {
        searchParams.append(key, value);
      }
    }
  }
  const query = searchParams.toString();
  return apiFetch<ProblemListDto[]>(
    `/v1/problems/items${query ? `?${query}` : ""}`,
  ).catch((error) => {
    console.error("Error fetching problems:", error);
    throw error;
  });
}

export function fetchTopics(): Promise<Topic[]> {
  return apiFetch<Topic[]>("/v1/topics").catch((error) => {
    console.error("Error fetching topics:", error);
    throw error;
  });
}

export function fetchProblem(problemId: number): Promise<Problem> {
  return apiFetch<Problem>(`/v1/problems/${problemId}`).catch((error) => {
    console.error("Error fetching problem:", error);
    throw error;
  });
}

export function fetchProblemBySlug(
  problemSlug: string,
): Promise<ProblemDto> {
  return apiFetch<ProblemDto>(`/v1/problems/slug/${problemSlug}`).catch(
    (error) => {
      console.error("Error fetching problem by slug:", error);
      throw error;
    },
  );
}

export function createProblem(title: string): Promise<Problem> {
  return apiFetch<Problem>("/v1/problems", {
    method: "POST",
    body: { title },
  }).catch((error) => {
    console.error("Error creating problem:", error);
    throw error;
  });
}

export function updateProblem(
  problemId: number,
  payload: ProblemUpdatePayload,
): Promise<Problem> {
  return apiFetch<Problem>(`/v1/problems/${problemId}`, {
    method: "PUT",
    body: payload,
  }).catch((error) => {
    console.error("Error updating problem:", error);
    throw error;
  });
}

export function deleteProblem(problemId: number): Promise<void> {
  return apiFetch<void>(`/v1/problems/${problemId}`, {
    method: "DELETE",
  }).catch((error) => {
    console.error("Error deleting problem:", error);
    throw error;
  });
}

export function publishProblem(problemId: number): Promise<void> {
  return apiFetch<void>(`/v1/problems/${problemId}/publish`, {
    method: "PATCH",
  }).catch((error) => {
    console.error("Error publishing problem:", error);
    throw error;
  });
}

export function unpublishProblem(problemId: number): Promise<void> {
  return apiFetch<void>(`/v1/problems/${problemId}/unpublish`, {
    method: "PATCH",
  }).catch((error) => {
    console.error("Error unpublishing problem:", error);
    throw error;
  });
}

export function fetchTestCases(problemId: number, isPublic?: boolean): Promise<TestCaseResponse[]> {
  const query = isPublic === undefined ? "" : `?isPublic=${isPublic}`;
  return apiFetch<TestCaseResponse[]>(
    `/v1/testCases/by-problem-id/${problemId}${query}`,
  ).catch((error) => {
    console.error("Error fetching test cases:", error);
    throw error;
  });
}

export function createTestCase(testCase: TestCaseRequest): Promise<TestCaseResponse> {
  return apiFetch<TestCaseResponse>("/v1/testCases", {
    method: "POST",
    body: testCase,
  }).catch((error) => {
    console.error("Error creating test case:", error);
    throw error;
  });
}

export function updateTestCase(
  id: string,
  testCase: TestCaseRequest,
): Promise<TestCaseResponse> {
  return apiFetch<TestCaseResponse>(`/v1/testCases/${id}`, {
    method: "PUT",
    body: testCase,
  }).catch((error) => {
    console.error("Error updating test case:", error);
    throw error;
  });
}

export function deleteTestCase(id: string): Promise<void> {
  return apiFetch<void>(`/v1/testCases/${id}`, { method: "DELETE" }).catch(
    (error) => {
      console.error("Error deleting test case:", error);
      throw error;
    },
  );
}
