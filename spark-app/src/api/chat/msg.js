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

/**
 * 分页查询消息列表
 * @param params 查询参数，含 spaceId、pageNo、pageSize
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMsgListAPI(params) {
  return request({
    url: '/chat/msg/pageList',
    method: 'get',
    params: params
  })
}
