import { createRouter, createWebHashHistory } from 'vue-router'
import layoutRout from './modules/layoutRout'
import login from '@/views/login'

/* 菜单栏的路由 */
// 固定菜单

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      component: login
    },
    ...layoutRout
  ],
  scrollBehavior (to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { left: 0, top: 0 }
    }
  }
})

export default router
