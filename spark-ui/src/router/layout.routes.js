// router/layout.routes.js
import index from '@/pages/index.vue'

// 根布局常量路由，模块子路由由权限守卫按后端菜单树动态挂载
export default [
    {
        path: '/',
        name: 'index',
        component: index,
        children: []
    }
]