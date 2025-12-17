import AdminView from '@/pages/AdminView.vue'
import MainView from '@/pages/MainView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/admin', component: AdminView },
    { path: '/', component: MainView },
  ],
})

export default router
