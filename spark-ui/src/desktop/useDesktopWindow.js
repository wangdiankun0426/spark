import {ref, onMounted, onBeforeUnmount} from 'vue'
import {isDesktop} from './env.js'

/**
 * Tauri 无边框窗口控制，仅桌面端可用，浏览器环境调用为空操作
 * @returns {{isMainWindow: import('vue').Ref<boolean>, isMaximized: import('vue').Ref<boolean>, minimize: Function, toggleMaximize: Function, hideWindow: Function}}
 */
export function useDesktopWindow() {
  // 仅主窗口使用自定义标题栏，子窗口保留系统标题栏
  const isMainWindow = ref(false)
  const isMaximized = ref(false)

  let unlistenResize
  let unlistenMaximize
  let unlistenUnmaximize

  function getCurrentWindow() {
    return window.__TAURI__?.window.getCurrentWindow()
  }

  // 浏览器调试态没有 Tauri 窗口 API，窗口操作一律降级为空操作
  function hasTauriApi() {
    return isDesktop && !!window.__TAURI__
  }

  async function syncMaximized() {
    if (!hasTauriApi()) return
    isMaximized.value = await getCurrentWindow().isMaximized()
  }

  async function minimize() {
    if (!hasTauriApi()) return
    await getCurrentWindow().minimize()
  }

  async function toggleMaximize() {
    if (!hasTauriApi()) return
    await getCurrentWindow().toggleMaximize()
  }

  // 关闭主窗口只隐藏到系统托盘，进程保留，点击托盘图标恢复，真正退出走托盘菜单
  async function hideWindow() {
    if (!hasTauriApi()) return
    await getCurrentWindow().hide()
  }

  onMounted(async () => {
    if (!isDesktop) return
    // 浏览器调试态按主窗口处理，让标题栏布局完整可见，窗口操作不生效
    if (!window.__TAURI__) {
      isMainWindow.value = true
      return
    }
    const appWindow = getCurrentWindow()
    isMainWindow.value = appWindow.label === 'main'
    await syncMaximized()
    unlistenResize = await appWindow.onResized(syncMaximized)
    unlistenMaximize = await appWindow.onMaximized(() => { isMaximized.value = true })
    unlistenUnmaximize = await appWindow.onUnmaximized(() => { isMaximized.value = false })
  })

  onBeforeUnmount(() => {
    unlistenResize?.()
    unlistenMaximize?.()
    unlistenUnmaximize?.()
  })

  return { isMainWindow, isMaximized, minimize, toggleMaximize, hideWindow }
}
