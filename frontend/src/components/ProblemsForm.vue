<script setup lang="ts">
import { ProblemDifficulty, useProblemsStore } from '@/stores/problems'
import { ref } from 'vue'

const problemsStore = useProblemsStore()
const title = ref<string>()
const difficultyItems = ref([
  { name: 'Easy', code: ProblemDifficulty.EASY },
  { name: 'Medium', code: ProblemDifficulty.MEDIUM },
  { name: 'Hard', code: ProblemDifficulty.HARD },
])
const selectedDifficulty = ref<ProblemDifficulty>()

function createProblem() {
  if (!title.value || !selectedDifficulty.value) return
  problemsStore.addProblem({
    title: title.value,
    difficulty: selectedDifficulty.value,
  })
  title.value = ''
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <p class="font-bold">Create problem</p>

    <InputText type="text" v-model="title" placeholder="Title" />
    <Select
      v-model="selectedDifficulty"
      :options="difficultyItems"
      option-label="name"
      option-value="code"
      placeholder="Difficulty"
    />

    <Button @click="createProblem" size="small" label="Submit" />
  </div>
</template>
