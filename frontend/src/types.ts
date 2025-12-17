export enum ProblemDifficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
}

export interface Problem {
  id: string
  name: string
  text: string
  difficulty: ProblemDifficulty
  constraints: string[]
  hints: string[]
  createdAt: Date
  updatedAt: Date
  publishedAt?: Date
}
