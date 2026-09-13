import request from '@/api/request'

/**
 * 分页查询当前用户消息列表
 * @param params 查询参数（pageNo/pageSize）
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyMessageListAPI(params) {
  return request({
    url: '/sys/message/pageMyList',
    method: 'get',
    params: params
  })
}