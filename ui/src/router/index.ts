import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import NewUserView from '@/views/NewUserView.vue'
import { keycloak } from '@/keycloak'
import { skipped } from '@/stores/skipped'
import { fetchPermissions, permissions } from '@/stores/permissions'
import GraphView from '@/views/GraphView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: DashboardView,
    },
    {
      path: '/new',
      name: 'new-user',
      component: NewUserView,
    },
    {
      path: '/graph/:id',
      name: 'graph',
      component: GraphView,
    },
  ],
})

router.beforeEach(async (to) => {
  await fetchPermissions()
  if (
    keycloak.authenticated &&
    to.name !== 'new-user' &&
    !skipped.value &&
    permissions.value.length === 0
  ) {
    return { name: 'new-user' }
  }
})

export default router
