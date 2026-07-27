import request from '@/api/request'

/**
 * 查询当前用户消息列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function myMessageListAPI() {
  return request({
    url: '/system/message/myList',
    method: 'get',
  })
}