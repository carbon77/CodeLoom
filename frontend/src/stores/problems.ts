import { defineStore } from 'pinia'

export enum ProblemDifficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
}

export type Problem = {
  title: string
  difficulty: ProblemDifficulty
}

export const useProblemsStore = defineStore('problems', {
  state() {
    return {
      problems: [] as Problem[],
      loading: false,
    }
  },
  actions: {
    addProblem(problem: Problem) {
      this.problems.push(problem)
    },
  },
})
