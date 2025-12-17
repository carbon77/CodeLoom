<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([{ label: 'Problems', to: '/admin/problems' }])
</script>

<template>
  <div class="flex items-start gap-3">
    <Menu :model="menuItems">
      <template #item="{ item }">
        <RouterLink class="flex items-center gap-2 px-3 py-1" :to="item.to">
          <span>{{ item.label }}</span>
        </RouterLink>
      </template>
    </Menu>
    <div class="flex-auto">
      <router-view />
    </div>
  </div>
</template>
