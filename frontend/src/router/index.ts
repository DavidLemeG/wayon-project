import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/agendar',
    },
    {
      path: '/agendar',
      name: 'agendar',
      component: () => import('../views/ScheduleTransferView.vue'),
    },
    {
      path: '/extrato',
      name: 'extrato',
      component: () => import('../views/StatementView.vue'),
    },
  ],
})

export default router
