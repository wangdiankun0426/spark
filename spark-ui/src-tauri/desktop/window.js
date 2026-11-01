import {ref, onMounted, onBeforeUnmount} from 'vue'
import {isDesktop} from './env.js'

/**
 * Tauri 无边框窗口控制：最小化、最大化/还原、关闭到托盘及最大化状态同步
 * 仅桌面端可用，浏览器环境调用为空操作
 * @returns {{desktopVisible: import('vue').Ref<boolean>, isMaximized: import('vue').Ref<boolean>, minimize: Function, toggleMaximize: Function, hideWindow: Function}}
 */
export function useDesktopWindow() {
  // 仅主窗口使用自定义标题栏（子窗口保留系统标题栏）
  const desktopVisible = ref(false)
  const isMaximized = ref(false)

  let unlistenResize
  let unlistenMaximize
  let unlistenUnmaximize

  function getCurrentWindow() {
    return window.__TAURI__.window.getCurrentWindow()
  }

  async function syncMaximized() {
    if (!isDesktop) return
    isMaximized.value = await getCurrentWindow().isMaximized()
  }

  async function minimize() {
    if (!isDesktop) return
    await getCurrentWindow().minimize()
  }

  async function toggleMaximize() {
    if (!isDesktop) return
    await getCurrentWindow().toggleMaximize()
  }

  // 关闭主窗口只隐藏到系统托盘，进程保留，点击托盘图标恢复，真正退出走托盘菜单
  async function hideWindow() {
    if (!isDesktop) return
    await getCurrentWindow().hide()
  }

  onMounted(async () => {
    if (!isDesktop) return
    const appWindow = getCurrentWindow()
    desktopVisible.value = appWindow.label === 'main'
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

  return { desktopVisible, isMaximized, minimize, toggleMaximize, hideWindow }
}
