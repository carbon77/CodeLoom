<script setup lang="ts">
import type ProblemsForm from '@/components/ProblemsForm.vue'
import { useProblemsStore } from '@/stores/problems'

const problemsStore = useProblemsStore()
const problems = problemsStore.problems
</script>

<template>
  <div class="flex gap-5">
    <DataTable class="w-1/2" stripedRows showGridlines :value="problems">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-bold">Problems</span>
        </div>
      </template>
      <Column field="title" header="Title" />
      <Column field="difficulty" header="Difficulty" />
    </DataTable>
    <div>
      <ProblemsForm />
    </div>
  </div>
</template>
