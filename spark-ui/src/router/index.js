// router/index.js
import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { isTauri } from '@/utils/desktop.js'

// 判断是否为桌面端环境（Tauri 需使用 Hash 模式兼容应用内协议）
const history = isTauri ? createWebHashHistory() : createWebHistory()

// 导入所有模块路由
import commonRoutes from './modules/common.routes'
import layoutRoutes from './modules/layout.routes'
import manageRoutes from './modules/manage.routes'
import llmRoutes from './modules/llm.routes.js'
import kbRoutes from './modules/kb.routes.js'
import flowRoutes from './modules/flow.routes'
import kgRoutes from './modules/kg.routes'

// 导入 utils
import { isAdmin } from '../utils/utils.js'

// 将所有需要挂载到 layout 的模块注入 children
layoutRoutes[0].children = [
    {
        path: '/home',
        name: 'home',
        component: () => import('@/pages/home/index'),
        meta: { title: '首页' },
        children: [
            { path: '/home/index', name: 'home', component: () => import('@/pages/home/index') }
        ]
    },
    ...manageRoutes,
    ...llmRoutes,
    ...kbRoutes,
    ...flowRoutes,
    ...kgRoutes,
]

// 合并所有路由
const routes = [
    ...commonRoutes,
    ...layoutRoutes
]

const router = createRouter({
    history,
    routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
    // 设置标题
    document.title = to.meta.title ? `星火云AI应用平台 - ${to.meta.title}` : '星火云AI应用平台'

    // 管理端权限校验
    if (to.path.startsWith('/manage')) {
        if (!isAdmin()) {
            return next('/noPermission')
        }
    }

    next()
})

export default router