/**
 * 防抖工具 - Vue自定义指令
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 */

/**
 * 防抖指令
 * 用法: v-debounce="handleClick" 或 v-debounce:1000="handleClick"
 */
export const debounce = {
  mounted(el, binding) {
    const delay = binding.arg ? parseInt(binding.arg) : 1000
    let timer = null
    el.addEventListener('click', () => {
      if (timer) {
        clearTimeout(timer)
      }
      timer = setTimeout(() => {
        if (typeof binding.value === 'function') {
          binding.value()
        }
        timer = null
      }, delay)
    })
  }
}

/**
 * 输入防抖指令
 * 用法: v-debounce-input="handleSearch" 或 v-debounce-input:500="handleSearch"
 */
export const debounceInput = {
  mounted(el, binding) {
    const delay = binding.arg ? parseInt(binding.arg) : 1000
    let timer = null
    // 获取实际input元素
    const inputEl = el.tagName === 'INPUT' ? el : el.querySelector('input')
    if (!inputEl) {
      console.warn('v-debounce-input: 未找到input元素')
      return
    }
    inputEl.addEventListener('input', (e) => {
      if (timer) {
        clearTimeout(timer)
      }
      timer = setTimeout(() => {
        if (typeof binding.value === 'function') {
          binding.value(e.target.value)
        }
        timer = null
      }, delay)
    })
  }
}

/**
 * 节流指令
 * 用法: v-throttle="handleScroll" 或 v-throttle:200="handleScroll"
 */
export const throttle = {
  mounted(el, binding) {
    const interval = binding.arg ? parseInt(binding.arg) : 1000
    let lastTime = 0
    el.addEventListener('click', () => {
      const now = Date.now()
      if (now - lastTime >= interval) {
        lastTime = now
        if (typeof binding.value === 'function') {
          binding.value()
        }
      }
    })
  }
}

/**
 * 安装所有指令到Vue实例
 * @param {Object} app Vue应用实例
 */
export function installDebounceDirectives(app) {
  app.directive('debounce', debounce)
  app.directive('debounce-input', debounceInput)
  app.directive('throttle', throttle)
}
