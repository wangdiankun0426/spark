import request from '@/api/request'

/**
 * 分页查询模型列表
 * @param query 查询参数
 * @returns {Promise}
 */
export function pageModelListAPI(query) {
  return request({
    url: '/llm/model/pageList',
    method: 'get',
    params: query
  })
}

/**
 * 查询模型详情
 * @param query 查询参数
 * @returns {Promise}
 */
export function queryModelDetailAPI(query) {
  return request({
    url: '/llm/model/detail',
    method: 'get',
    params: query
  })
}
