import request from '@/api/request'

/**
 * 获取未读消息列表
 * @param params
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getNoReadMsgListAPI(params) {
  return request({
    url: '/chat/msg/noReadList',
    method: 'get',
    params: params
  })
}
