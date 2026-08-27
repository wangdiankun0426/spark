/**
 * 防抖工具
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 */

/**
 * Vue 组合式 API：防抖函数
 * @param {Function} fn 需要防抖的函数
 * @param {number} delay 延迟时间（毫秒），默认 1000
 * @returns {Function} 防抖后的函数
 */
export function useDebounceFn(fn, delay = 1000) {
  let timer = null
  const debouncedFn = function (...args) {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
  // 取消防抖
  debouncedFn.cancel = function () {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }
  // 立即执行
  debouncedFn.flush = function (...args) {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    fn.apply(this, args)
  }
  return debouncedFn
}

/**
 * 节流函数
 * @param {Function} fn 需要节流的函数
 * @param {number} interval 间隔时间（毫秒），默认 1000
 * @returns {Function} 节流后的函数
 */
export function useThrottleFn(fn, interval = 1000) {
  let lastTime = 0
  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= interval) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}
