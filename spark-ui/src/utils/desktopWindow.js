import {ref, onMounted, onBeforeUnmount} from 'vue'
import {ElMessage} from 'element-plus'
import {isDesktop} from '@/utils/desktop.js'

/**
 * Tauri 无边框窗口控制：最小化、最大化/还原、关闭及最大化状态同步
 * 仅桌面端可用，浏览器环境调用为空操作
 * @returns {{desktopVisible: import('vue').Ref<boolean>, isMaximized: import('vue').Ref<boolean>, minimize: Function, toggleMaximize: Function, close: Function}}
 */
export function useDesktopWindow() {
  // 仅主窗口使用自定义标题栏（子窗口保留系统标题栏）
  const desktopVisible = ref(false)
  const isMaximized = ref(false)
  const isFullscreen = ref(false)

  let unlistenResize
  let unlistenMaximize
  let unlistenUnmaximize
  let unlistenFullscreenChange

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

  async function close() {
    if (!isDesktop) return
    await getCurrentWindow().close()
  }

  async function toggleFullscreen() {
    if (!isDesktop) {
      if (document.fullscreenElement) {
        document.exitFullscreen().catch(() => {})
      } else {
        document.documentElement.requestFullscreen().catch(() => {
          ElMessage.warning('当前环境不支持全屏')
        })
      }
      return
    }
    isFullscreen.value = !isFullscreen.value
    await getCurrentWindow().setFullscreen(isFullscreen.value)
  }

  onMounted(async () => {
    if (!isDesktop) return
    const appWindow = getCurrentWindow()
    desktopVisible.value = appWindow.label === 'main'
    await syncMaximized()
    isFullscreen.value = await appWindow.isFullscreen()
    unlistenResize = await appWindow.onResized(syncMaximized)
    unlistenMaximize = await appWindow.onMaximized(() => { isMaximized.value = true })
    unlistenUnmaximize = await appWindow.onUnmaximized(() => { isMaximized.value = false })
    unlistenFullscreenChange = await appWindow.onFullscreenChanged(({ state }) => {
      isFullscreen.value = state
    })
  })

  onBeforeUnmount(() => {
    unlistenResize?.()
    unlistenMaximize?.()
    unlistenUnmaximize?.()
    unlistenFullscreenChange?.()
  })

  return { desktopVisible, isMaximized, isFullscreen, minimize, toggleMaximize, toggleFullscreen, close }
}
