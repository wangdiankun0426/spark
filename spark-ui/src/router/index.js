// router/index.js
import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { isDesktop } from '@desktop/env.js'
import store from '@/store/index.js'
import { getSessionAPI } from '@/api/manage/auth/login.js'
import commonRoutes from './common.routes.js'
import layoutRoutes from './layout.routes.js'

// 桌面端用 Hash 模式兼容应用内协议
const history = isDesktop ? createWebHashHistory() : createWebHistory()

const router = createRouter({
    history,
    routes: [...commonRoutes, ...layoutRoutes]
})

// 免登录白名单
const whiteList = ['/login', '/oauthLogin', '/register', '/agreement']

// 清除上一身份残留的动态路由，避免切换租户或重登后旧授权路由仍可直达
function clearDynamicRoutes() {
    router.getRoutes().forEach(route => {
        if (route.name && route.meta && route.meta.code && router.hasRoute(route.name)) {
            router.removeRoute(route.name)
        }
    })
}

router.beforeEach(async (to, from, next) => {
    document.title = to.meta.title ? `星火云应用平台 - ${to.meta.title}` : '星火云应用平台'
    // 白名单页直接放行，登录页由此挂载清残留凭证，破解 601 跳转后的循环
    if (whiteList.includes(to.path)) {
        return next()
    }
    const token = store.getters['user/getToken']
    if (!token) {
        return next('/login')
    }
    // 动态路由未生成时拉取会话并挂载，刷新与切换租户后由此恢复
    if (!store.getters['permission/routesGenerated']) {
        try {
            const res = await getSessionAPI()
            if (!res || res.code !== 200) {
                store.dispatch('user/logout')
                return next('/login')
            }
            store.dispatch('user/setUserInfo', { userInfo: res.data })
            const accessRoutes = await store.dispatch('permission/generateRoutes', {
                routers: res.data.routers,
                permissions: res.data.permissions
            })
            clearDynamicRoutes()
            accessRoutes.forEach(route => router.addRoute('index', route))
            // 按路径重解析，spread to 会带上刷新时 catchAll 的 noFound 名导致复匹到 404
            return next({ path: to.path, query: to.query, hash: to.hash, replace: true })
        } catch (e) {
            store.dispatch('user/logout')
            return next('/login')
        }
    }
    if (to.path === '/') {
        const defaultPath = store.getters['permission/defaultPath']
        return next(defaultPath || '/noPermission')
    }
    // 已登录但目标不在授权路由内，直达无权页
    if (to.name === 'noFound') {
        return next('/noPermission')
    }
    next()
})

export default router