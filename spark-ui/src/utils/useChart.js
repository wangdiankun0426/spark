import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * ECharts生命周期组合式函数，统一处理init/resize/dispose
 * @returns {{elRef: import('vue').Ref, setOption: Function}}
 */
export function useChart() {
  const elRef = ref(null)
  let chart = null

  function ensureChart() {
    if (!chart && elRef.value) {
      chart = echarts.init(elRef.value)
    }
    return chart
  }

  function setOption(option) {
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

  onMounted(() => {
    window.addEventListener('resize', resize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    if (chart) {
      chart.dispose()
      chart = null
    }
  })

  return { elRef, setOption }
}
