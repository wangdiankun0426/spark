import request from '@/api/request'

/**
 * 查询我的用量汇总
 * @param params rangeDays 统计天数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryUsageSummaryAPI(params) {
  return request({ url: '/home/usageSummary', method: 'get', params })
}

/**
 * 查询我的用量趋势
 * @param params rangeDays 统计天数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryUsageTrendAPI(params) {
  return request({ url: '/home/usageTrend', method: 'get', params })
}

/**
 * 查询我的待办总数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryTodoTotalAPI() {
  return request({ url: '/home/todoTotal', method: 'get' })
}

/**
 * 查询我的待办列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryTodoListAPI() {
  return request({ url: '/home/todoList', method: 'get' })
}

/**
 * 查询我的流程四项数量（待办/已办/申请/抄送）
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryFlowCountAPI() {
  return request({ url: '/home/flowCount', method: 'get' })
}

/**
 * 查询可申请流程模板
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryApplyTemplatesAPI() {
  return request({ url: '/home/applyTemplates', method: 'get' })
}

/**
 * 查询综合推荐榜
 * @param params rangeDays 统计天数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryRecommendAPI(params) {
  return request({ url: '/home/recommend', method: 'get', params })
}

/**
 * 查询我的资产数量
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryAssetAPI() {
  return request({ url: '/home/asset', method: 'get' })
}
