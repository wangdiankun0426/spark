import request from '@/api/request'

/**
 * 查询用户列表（通讯录）
 * @param params
 * @returns {Promise<AxiosResponse<any>>}
 */
export function userMyListAPI(params) {
  return request({
    url: '/chat/user/myList',
    method: 'get',
    params: params
  })
}
