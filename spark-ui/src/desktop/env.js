/**
 * 桌面端环境适配，浏览器环境下 isDesktop 为 false，下面的能力全部不注册；
 * Tauri 会在页面脚本执行前注入 window.__TAURI_INTERNALS__ / window.__TAURI__，以此作为环境标识
 */
export const isDesktop = typeof window !== 'undefined' && (
    window.__TAURI_INTERNALS__ !== undefined ||
    // 开发模式下允许在浏览器里用 ?__desktop=1 强制进入客户端形态，便于单独调试客户端外壳样式；
    // 该分支依赖 import.meta.env.DEV，打包后会被整体移除
    (import.meta.env.DEV && new URLSearchParams(window.location.search).has('__desktop'))
)

/**
 * 将任意内部地址规范化为 Hash 路由
 * '/document/list'           -> '#/document/list'
 * '#/document/preview?id=1'  -> 原样返回
 * '/index.html#/xxx'         -> '#/xxx'
 */
function toHashRoute(url) {
  let u = String(url)
  const hashIndex = u.indexOf('#')
  if (hashIndex >= 0) {
    return u.slice(hashIndex)
  }
  if (!u.startsWith('/')) {
    u = '/' + u
  }
  return '#' + u
}

/**
 * 初始化桌面端适配：接管 window.open
 *
 * Tauri 下 window.open 无法直接创建新窗口，统一接管：
 * - 内部路由 -> 调用 Rust 命令 open_app_window 创建新应用窗口，localStorage 同源共享登录态
 * - 外部 http 或 https 链接 -> 调用 Rust 命令 open_external_url 交给系统默认浏览器
 *
 * 同时在 <html> 上挂 desktop-shell 类，desktop.scss 的全部规则以该类为前缀，
 * 浏览器端无此类名，随包引入不会影响 Web 版表现。
 */
export function setupDesktop() {
  if (!isDesktop) {
    return
  }
  document.documentElement.classList.add('desktop-shell')
  // 正式包禁用右键菜单，开发模式保留调试能力
  if (!import.meta.env.DEV) {
    document.addEventListener('contextmenu', (e) => {
      e.preventDefault()
    })
  }
  const rawOpen = window.open.bind(window)
  window.open = (url) => {
    if (url === undefined || url === null || url === '') {
      return rawOpen(url)
    }
    // 浏览器调试态没有 Tauri API，交回原生实现
    if (!window.__TAURI__) {
      return rawOpen(url)
    }
    const invoke = window.__TAURI__.core.invoke
    if (/^https?:\/\//i.test(url)) {
      invoke('open_external_url', { url })
    } else {
      invoke('open_app_window', { route: toHashRoute(url) })
    }
    return null
  }
}
