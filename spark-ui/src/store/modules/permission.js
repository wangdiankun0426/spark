/**
 * 权限模块 - 后端菜单树驱动动态路由与操作权限
 */
import { isDesktop } from '@desktop/env.js'

// 懒加载页面组件映射，键为相对 src 无后缀路径，与后端菜单 component 串一致
const pageModules = import.meta.glob('@/pages/**/*.vue')
const viewModules = import.meta.glob('@/views/**/*.vue')
const componentMap = {}
const allModules = { ...pageModules, ...viewModules }
Object.keys(allModules).forEach(key => {
  const idx = key.indexOf('src/')
  const rel = idx >= 0 ? key.slice(idx + 4) : key.replace(/^@\//, '')
  componentMap[rel.replace(/\.vue$/, '')] = allModules[key]
})

const state = {
  permissions: [],
  menuTree: [],
  routes: [],
  routesGenerated: false
}

const mutations = {
  SET_PERMISSIONS(state, permissions) {
    state.permissions = permissions
  },
  SET_MENU_TREE(state, menuTree) {
    state.menuTree = menuTree
  },
  SET_ROUTES(state, routes) {
    state.routes = routes
  },
  SET_ROUTES_GENERATED(state, generated) {
    state.routesGenerated = generated
  },
  RESET(state) {
    state.permissions = []
    state.menuTree = []
    state.routes = []
    state.routesGenerated = false
  }
}

const actions = {
  /**
   * 由后端菜单树生成动态路由并缓存权限串
   * @param routers 后端菜单树
   * @param permissions 操作权限串集合
   * @returns {Promise<Array>} 可挂载的模块路由
   */
  generateRoutes({ commit }, { routers, permissions }) {
    const menuTree = routers || []
    commit('SET_PERMISSIONS', permissions || [])
    commit('SET_MENU_TREE', menuTree)
    const accessRoutes = buildRoutes(menuTree)
    commit('SET_ROUTES', accessRoutes)
    commit('SET_ROUTES_GENERATED', true)
    return Promise.resolve(accessRoutes)
  },
  reset({ commit }) {
    commit('RESET')
  }
}

const getters = {
  permissions: state => state.permissions,
  menuTree: state => state.menuTree,
  routes: state => state.routes,
  routesGenerated: state => state.routesGenerated,
  // 落地页取首个模块的首个可达菜单，站点管理员无首页模块时自然落到站点管理
  defaultPath: state => {
    const first = state.routes.find(route => route.redirect)
    return first ? first.redirect : undefined
  },
  hasPermi: state => perm => {
    return state.permissions.includes('*:*:*') || state.permissions.includes(perm)
  }
}

/**
 * 解析组件串为懒加载函数，未命中告警并跳过
 * @param component 相对 src 无后缀组件路径
 * @returns {Function|undefined}
 */
function resolveComponent(component) {
  if (!component) {
    return undefined
  }
  const loader = componentMap[component]
  if (!loader) {
    console.warn('route component miss, component=' + component)
    return undefined
  }
  return loader
}

/**
 * 将模块菜单树转为 vue-router 模块路由，分组目录拍平为平铺子路由
 * @param menuTree 后端菜单树
 * @returns {Array}
 */
function buildRoutes(menuTree) {
  const routes = []
  menuTree.forEach(mod => {
    if (!mod.path) {
      return
    }
    const children = []
    collectLeafMenus(mod.children || [], children)
    const route = {
      path: mod.path,
      name: mod.code,
      redirect: firstReachable(children),
      meta: { title: mod.title, icon: mod.icon, code: mod.code },
      children
    }
    // 桌面端首页由独立外壳承担导航，避免双份侧栏
    if (mod.code === 'module.home' && isDesktop) {
      route.component = () => import('@desktop/pages/homeContainer.vue')
    } else {
      route.component = resolveComponent(mod.component)
    }
    // 模块组件缺失时跳过，避免挂载空组件路由致白屏
    if (!route.component) {
      return
    }
    routes.push(route)
  })
  return routes
}

/**
 * 递归收集叶子菜单为平铺子路由，分组目录无组件仅向下递归
 * @param nodes 菜单节点数组
 * @param out 收集结果数组
 */
function collectLeafMenus(nodes, out) {
  nodes.forEach(node => {
    if (node.path && node.component) {
      const comp = resolveComponent(node.component)
      if (comp) {
        out.push({
          path: node.path,
          name: node.code,
          component: comp,
          meta: { title: node.title, hidden: node.hidden, icon: node.icon, code: node.code }
        })
      }
    }
    if (node.children && node.children.length) {
      collectLeafMenus(node.children, out)
    }
  })
}

/**
 * 取首个非隐藏子路由作为模块重定向目标
 * @param children 子路由数组
 * @returns {string|undefined}
 */
function firstReachable(children) {
  const target = children.find(child => !child.meta.hidden)
  return target ? target.path : undefined
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}