import request from '@/api/request'

/**
 * 创建聊天空间
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createChatSpaceAPI(data) {
  return request({
    url: '/chat/space/create',
    method: 'post',
    params: data
  })
}
