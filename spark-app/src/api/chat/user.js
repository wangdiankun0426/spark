import request from '@/api/request'

/**
 * 分页查询我的聊天用户列表，带会话空间id与未读数
 * @param params 查询参数，含 pageNo、pageSize、name
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageUserMyListAPI(params) {
  return request({
    url: '/chat/user/pageMyList',
    method: 'get',
    params: params
  })
}
