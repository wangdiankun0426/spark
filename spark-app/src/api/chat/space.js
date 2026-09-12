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

/**
 * 创建AI会话
 */
export function createAiChatSpaceAPI(data) {
  return request({
    url: '/chat/space/aiCreate',
    method: 'post',
    params: data
  })
}

/**
 * 分页查询我的AI会话列表，支持按接收人与标题检索
 * @param params 查询参数，含 spaceType、receiverId、title、pageNo、pageSize
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyChatSpaceListAPI(params) {
  return request({
    url: '/chat/space/pageMyList',
    method: 'get',
    params: params
  })
}
