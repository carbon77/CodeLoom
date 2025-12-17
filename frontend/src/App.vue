<script setup lang="ts">
import { ref } from 'vue'

const navbarItems = ref([
  {
    label: 'Home',
    icon: 'pi pi-home',
    to: '/',
  },
  {
    label: 'Admin',
    icon: 'pi pi-user',
    to: '/admin',
  },
])
</script>

<template>
  <div class="p-3 flex flex-col gap-3">
    <header>
      <Menubar :model="navbarItems">
        <template #item="{ item }">
          <RouterLink class="flex items-center gap-2 px-3 py-1" :to="item.to">
            <i :class="item.icon"></i>
            <span>{{ item.label }}</span>
          </RouterLink>
        </template>
      </Menubar>
    </header>
    <main><RouterView /></main>
  </div>
</template>
