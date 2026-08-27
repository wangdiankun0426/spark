import request from '@/api/request'

/**
 * 分页查询厂商列表
 * @param query 查询参数
 * @returns {Promise}
 */
export function pageProviderListAPI(query) {
  return request({
    url: '/llm/provider/pageList',
    method: 'get',
    params: query
  })
}
