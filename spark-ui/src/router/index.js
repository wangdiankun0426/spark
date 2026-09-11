// router/index.js
import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { isDesktop } from '@/utils/desktop.js'
import {isOrgAdmin, isSysAdmin} from '../utils/utils.js'
import {hasMenu} from "@/utils/menuUtil.js";

// 判断是否为桌面端环境（桌面端环境 需使用 Hash 模式兼容应用内协议）
const history = isDesktop ? createWebHashHistory() : createWebHistory()

// 导入所有模块路由
import commonRoutes from './common.routes.js'
import layoutRoutes from './layout.routes.js'

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
router.beforeEach(async (to, from, next) => {
    // 设置标题
    document.title = to.meta.title ? `星火云应用平台 - ${to.meta.title}` : '星火云应用平台'
    // 根据角色分发路由
    if (to.path === '/') {
        if (isOrgAdmin()) {
            return next('/org')
        } else if (isSysAdmin()) {
            return next('/site')
        } else {
            return next('/home')
        }
    }
    // 系统后台权限校验
    if (to.path.startsWith('/site')) {
        if (!isSysAdmin()) {
            return next('/noPermission')
        }
    }
    // 组织管理权限校验
    if (to.path.startsWith('/org')) {
        if (!isOrgAdmin()) {
            return next('/noPermission')
        }
    }
    // 菜单权限校验：需要菜单权限的路由，无权直达跳无权限页
    if (to.meta.menuCode) {
        if (!hasMenu(to.meta.menuCode)) {
            return next('/noPermission')
        }
    }
    next()
})

export default router