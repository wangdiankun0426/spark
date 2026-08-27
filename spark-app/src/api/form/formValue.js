import request from '@/api/request'

/**
 * 查询表单数据
 * @param query 查询参数 {objId: 实例ID}
 * @returns {Promise}
 */
export function detailFormValueAPI(query) {
  return request({
    url: '/form/value/detail',
    method: 'get',
    params: query
  })
}
