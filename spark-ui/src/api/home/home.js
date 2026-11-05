import request from '@/api/request'

/**
 * 查询工作台总览，含模型用量与资产数量
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryOverviewAPI() {
  return request({ url: '/home/overview', method: 'get' })
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
 * 查询我的流程四项数量（待办/已办/申请/抄送）
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryFlowCountAPI() {
  return request({ url: '/home/flowCount', method: 'get' })
}

/**
 * 查询综合推荐榜
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryRecommendAPI() {
  return request({ url: '/home/recommend', method: 'get' })
}
