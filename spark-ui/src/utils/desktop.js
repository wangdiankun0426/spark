/**
 * 桌面端环境适配
 *
 * 仅在 Tauri Webview 中生效，浏览器环境完全不受影响。
 * Tauri 在页面脚本执行前注入 window.__TAURI_INTERNALS__ / window.__TAURI__，
 * 以此作为桌面端环境标识（WebView2 的 userAgent 不含 electron 等特征）。
 */

// 是否为桌面端环境
export const isDesktop = typeof window !== 'undefined' && window.__TAURI_INTERNALS__ !== undefined

/**
 * 将任意内部地址规范化为 hash 路由（桌面端使用 Hash 路由）
 * '/notice/view/1'           -> '#/notice/view/1'
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
 * - 内部路由 -> 调用 Rust 命令 open_app_window 创建新应用窗口（localStorage 同源共享登录态）
 * - 外部 http(s) 链接 -> 调用 Rust 命令 open_external_url 交给系统默认浏览器
 */
export function setupDesktop() {
    if (!isDesktop) {
        return
    }
    const rawOpen = window.open.bind(window)
    window.open = (url) => {
        // 空 url（如 window.open()、about:blank）保持原生行为
        if (url === undefined || url === null || url === '') {
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
