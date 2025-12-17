import ProblemsTable from '@/components/ProblemsTable.vue'
import AdminView from '@/pages/AdminView.vue'
import EditProblemView from '@/pages/EditProblemView.vue'
import MainView from '@/pages/MainView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: MainView },
    {
      path: '/admin',
      component: AdminView,
      children: [
        { path: 'problems', component: ProblemsTable },
        { path: 'problems/:id', name: 'problem', component: EditProblemView },
      ],
    },
  ],
})

export default router
