import { reactive } from 'vue'

// 自定义导航栏所需的状态栏与微信胶囊布局，全局只计算一次
export const navBarInfo = reactive({
  statusBarHeight: 0,
  menuTop: 22,
  menuBottom: 56,
  menuHeight: 34,
  menuWidth: 0,
  menuPaddingRight: 12
})

let initialized = false

/**
 * 初始化导航栏布局信息，仅微信小程序计算，H5 沿用默认值
 */
export function initNavBarInfo() {
  if (initialized) {
    return
  }
  initialized = true
  // #ifdef MP-WEIXIN
  const systemInfo = uni.getSystemInfoSync()
  navBarInfo.statusBarHeight = systemInfo.statusBarHeight || 0
  const menuRect = uni.getMenuButtonBoundingClientRect()
  navBarInfo.menuTop = menuRect.top
  navBarInfo.menuBottom = menuRect.bottom
  navBarInfo.menuHeight = menuRect.height
  navBarInfo.menuWidth = menuRect.width
  navBarInfo.menuPaddingRight = systemInfo.windowWidth - menuRect.right
  // #endif
}

/**
 * 蓝色头部的沉浸式内边距，小程序端撑到状态栏底部并与胶囊按钮对齐
 * @returns {Object} 头部绑定样式
 */
export function navHeaderStyle() {
  // #ifdef MP-WEIXIN
  if (navBarInfo.statusBarHeight) {
    return {paddingTop: navBarInfo.statusBarHeight + 16 + 'px'}
  }
  // #endif
  return {}
}

/**
 * 头部内容行下移到微信胶囊按钮下方，用于头部右侧自带操作按钮的页面
 * @param gap 与胶囊按钮底边的间距，默认 8px
 * @returns {Object} 头部绑定样式
 */
export function navHeaderBelowStyle(gap = 8) {
  // #ifdef MP-WEIXIN
  if (navBarInfo.statusBarHeight) {
    return {paddingTop: navBarInfo.menuBottom + gap + 'px'}
  }
  // #endif
  return {}
}

/**
 * 普通内容页根容器的顶部安全区内边距
 * @returns {Object} 根容器绑定样式
 */
export function safeAreaStyle() {
  if (!navBarInfo.statusBarHeight) {
    return {}
  }
  return {paddingTop: navBarInfo.statusBarHeight + 'px'}
}
