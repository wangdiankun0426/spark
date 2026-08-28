import request from '@/api/request'

/**
 * 分页查询检索日志
 * @param params 查询参数
 * @returns {Promise}
 */
export const getRetrieveLogPageListAPI = (params) => {
  return request({
    url: '/kb/retrieveLog/pageList',
    method: 'get',
    params
  })
}

/**
 * 查询检索统计
 * @param params 查询参数
 * @returns {Promise}
 */
export const getRetrieveStatsAPI = (params) => {
  return request({
    url: '/kb/retrieveLog/stats',
    method: 'get',
    params
  })
}

/**
 * 检索反馈
 * @param params 反馈数据
 * @returns {Promise}
 */
export const submitFeedbackAPI = (params) => {
  return request({
    url: '/kb/retrieveLog/feedback',
    method: 'post',
    params
  })
}

/**
 * 查询使用情况统计
 * @returns {Promise}
 */
export const getUsageStatsAPI = () => {
  return request({
    url: '/kb/retrieveLog/usageStats',
    method: 'get'
  })
}