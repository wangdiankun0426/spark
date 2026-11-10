import request from '@/api/request'

/**
 * 分页查询运行实例列表
 * @param query 查询参数
 * @returns {Promise}
 */
export function pageInstanceListAPI(query) {
  return request({
    url: '/workflow/instance/pageList',
    method: 'get',
    params: query
  })
}

/**
 * 查询运行实例详情
 * @param query 查询参数
 * @returns {Promise}
 */
export function queryInstanceDetailAPI(query) {
  return request({
    url: '/workflow/instance/detail',
    method: 'get',
    params: query
  })
}

/**
 * 查询运行节点记录
 * @param query 查询参数
 * @returns {Promise}
 */
export function queryInstanceNodesAPI(query) {
  return request({
    url: '/workflow/instance/nodes',
    method: 'get',
    params: query
  })
}

/**
 * 运行工作流
 * @param data 运行参数
 * @returns {Promise}
 */
export function runWorkflowAPI(data) {
  return request({
    url: '/workflow/instance/run',
    method: 'post',
    data: data
  })
}

/**
 * 分页查询待我审核实例
 * @param query 查询参数
 * @returns {Promise}
 */
export function pageMyReviewListAPI(query) {
  return request({
    url: '/workflow/instance/pageMyReview',
    method: 'get',
    params: query
  })
}

/**
 * 提交人工审核
 * @param data 审核参数，instanceId/approved/comment
 * @returns {Promise}
 */
export function reviewWorkflowInstanceAPI(data) {
  return request({
    url: '/workflow/instance/review',
    method: 'post',
    params: data
  })
}
