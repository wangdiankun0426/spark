import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { registerAppChartTheme, currentChartTheme } from '@/utils/chartTheme'

registerAppChartTheme()

/**
 * ECharts生命周期组合式函数，统一处理init/resize/dispose，并随深色主题切换重建实例
 * @returns {{elRef: import('vue').Ref, setOption: Function}}
 */
export function useChart() {
  const elRef = ref(null)
  let chart = null
  let lastOption = null

  function ensureChart() {
    if (!chart && elRef.value) {
      chart = echarts.init(elRef.value, currentChartTheme())
    }
    return chart
  }

  function setOption(option) {
    lastOption = option
    const instance = ensureChart()
    if (instance) {
      instance.setOption(option, true)
    }
  }

  function resize() {
    if (chart) {
      chart.resize()
    }
  }

  // 主题切换：重新注册主题色后重建实例并重放最后一次 option
  function handleThemeChange() {
    if (!chart || !lastOption) {
      return
    }
    chart.dispose()
    chart = null
    registerAppChartTheme()
    const instance = ensureChart()
    if (instance) {
      instance.setOption(lastOption, true)
      instance.resize()
    }
  }

  onMounted(() => {
    window.addEventListener('resize', resize)
    window.addEventListener('app-theme-change', handleThemeChange)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    window.removeEventListener('app-theme-change', handleThemeChange)
    if (chart) {
      chart.dispose()
      chart = null
    }
  })

  return { elRef, setOption }
}
