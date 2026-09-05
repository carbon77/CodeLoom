import type { ProblemDetail } from '../api/problems'

export const exampleProblem: ProblemDetail = {
  id: 7, slug: 'two_sum', title: 'Two Sum', difficulty: 'EASY',
  description: 'Find two numbers that add up to the target.\n\n```python\nprint(1 + 2)\n```',
  constraints: { executionTimeLimitMs: 1000, memoryUsageLimitMb: 256 },
  hints: ['Use a map'], topics: [{ id: 'arrays', name: 'Arrays' }],
  examples: [{ id: 'example', input: '1 2', expectedOutput: '3', isPublic: true }],
  testCases: [],
}
