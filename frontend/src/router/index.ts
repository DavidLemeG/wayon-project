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
    {
      // Sem esta rota, um caminho inexistente renderiza a area de conteudo
      // vazia: o menu aparece, o conteudo some, e o usuario nao sabe se a
      // pagina nao existe ou se a aplicacao travou.
      path: '/:pathMatch(.*)*',
      name: 'nao-encontrado',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

export default router
