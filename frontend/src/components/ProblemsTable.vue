<script setup lang="ts">
import { api } from '@/api'
import { type Problem } from '@/types'
import { useAxios } from '@vueuse/integrations/useAxios'
import { AxiosError } from 'axios'
import { useToast } from 'primevue'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const { isFinished, data, error, execute } = useAxios<Problem[]>('/problems', api)
const errorMessage = computed(() => {
  if (error.value instanceof AxiosError) {
    return error.value.message
  } else {
    return error
  }
})
const difficultyBadgeColor: Record<string, string> = {
  EASY: 'success',
  MEDIUM: 'warn',
  HARD: 'danger',
}

const convertDate = (date: Date) => new Date(date).toLocaleDateString()
const toast = useToast()

const newProblemName = ref('')
const createProblemLoading = ref(false)
const createProblem = async () => {
  createProblemLoading.value = true
  await api
    .post('/problems', { name: newProblemName.value })
    .then((resp) => {
      const id = resp.data.id
      router.push({
        name: 'problem',
        params: { id },
      })
    })
    .catch((err) => {
      toast.add({
        severity: 'error',
        summary: 'Failed to create problem!',
        detail: err?.message || 'Unknown error',
        group: 'br',
      })
    })
}
const editProblem = (id: string) =>
  router.push({
    name: 'problem',
    params: { id },
  })
</script>

<template>
  <Toast />
  <div class="flex items-start gap-5">
    <Panel class="flex-auto">
      <template #header>
        <div class="w-full flex justify-between items-center">
          <h1 class="text-lg font-bold">Problems</h1>
          <div class="flex gap-3">
            <Button icon="pi pi-refresh" @click="execute()" rounded />
          </div>
        </div>
      </template>
      <template v-if="error">{{ errorMessage }}</template>
      <template v-else>
        <DataTable :loading="!isFinished" :value="data" data-key="id" striped-rows>
          <Column field="name" header="Name" />
          <Column field="difficulty" header="Difficulty">
            <template #body="{ data }">
              <Badge :severity="difficultyBadgeColor[data.difficulty]" :value="data.difficulty" />
            </template>
          </Column>
          <Column header="Created">
            <template #body="{ data }">
              {{ convertDate(data.createdAt) }}
            </template>
          </Column>
          <Column header="Updated">
            <template #body="{ data }">
              {{ convertDate(data.updatedAt) }}
            </template>
          </Column>
          <Column field="publishedAt" header="Published">
            <template #body="{ data }">
              <Button
                v-if="data.publishedAt"
                label="Published"
                severity="success"
                outlined
                size="small"
              />
              <Button v-else label="Unpublished" severity="danger" outlined size="small" />
            </template>
          </Column>
          <Column header="Edit">
            <template #body="{ data }">
              <Button
                icon="pi pi-pencil"
                raised
                rounded
                size="small"
                @click="editProblem(data.id)"
              />
            </template>
          </Column>
        </DataTable>
      </template>
    </Panel>
    <Panel header="Create problem">
      <div class="flex flex-col gap-3">
        <InputText v-model="newProblemName" placeholder="Name" />
        <Button
          :loading="createProblemLoading"
          :disabled="!newProblemName"
          label="Create"
          @click="createProblem"
        />
      </div>
    </Panel>
  </div>
</template>
