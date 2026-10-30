import * as echarts from 'echarts'
import { getTheme } from '@/utils/themeUtil'

/**
 * 读取 html 根节点上的 CSS 变量计算值
 * @param name 变量名（含 -- 前缀）
 * @returns {string}
 */
export function getCssVar(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim()
}

/**
 * 图表中性色 token，供业务 option 构建时取色（canvas 不识别 var()，必须读计算值）
 * @returns {{text: string, axis: string, split: string, primary: string, tooltipBg: string}}
 */
export function chartTokens() {
  return {
    text: getCssVar('--app-text-secondary'),
    primary: getCssVar('--app-primary'),
    split: getCssVar('--app-border-light'),
    tooltipBg: getCssVar('--app-bg-overlay')
  }
}

/**
 * 注册 app-dark 图表主题（按当前 CSS 变量实时取值，需在主题切换后重新注册）
 */
export function registerAppChartTheme() {
  const tokens = chartTokens()
  echarts.registerTheme('app-dark', {
    backgroundColor: 'transparent',
    color: ['#4d8dff', '#34c759', '#ff9500', '#af52de', '#00c7be', '#ff6b9d', '#5e72e4'],
    textStyle: { color: tokens.text },
    categoryAxis: {
      axisLine: { lineStyle: { color: tokens.split } },
      axisTick: { lineStyle: { color: tokens.split } },
      axisLabel: { color: tokens.text },
      splitLine: { lineStyle: { color: tokens.split } }
    },
    valueAxis: {
      axisLine: { lineStyle: { color: tokens.split } },
      axisTick: { lineStyle: { color: tokens.split } },
      axisLabel: { color: tokens.text },
      splitLine: { lineStyle: { color: tokens.split } }
    },
    legend: { textStyle: { color: tokens.text } },
    tooltip: {
      backgroundColor: tokens.tooltipBg,
      borderColor: getCssVar('--app-border'),
      textStyle: { color: getCssVar('--app-text-primary') }
    }
  })
}

/**
 * 当前主题对应的 echarts 主题名
 * @returns {string|undefined}
 */
export function currentChartTheme() {
  return getTheme() === 'dark' ? 'app-dark' : undefined
}
