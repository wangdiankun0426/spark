import request from '@/api/request.js'

/**
 * 查询用户详情
 * @returns {Promise<AxiosResponse<any>>}
 */
export function userDetailAPI(query) {
  return request({
    url: '/sys/user/detail',
    method: 'get',
    params: query
  })
}

/**
 * 分页查询用户列表
 * @param query
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageUserListAPI(query)  {
  return request({
    url: '/sys/user/pageList',
    method: 'get',
    params: query
  })
}

/**
 * 创建用户
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createUserAPI(data)  {
  return request({
    url: '/sys/user/create',
    method: 'post',
    params: data
  })
}

/**
 * 修改用户
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateUserAPI(data)  {
  return request({
    url: '/sys/user/update',
    method: 'post',
    params: data
  })
}

/**
 * 删除用户
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteUserAPI(data)  {
  return request({
    url: '/sys/user/delete',
    method: 'post',
    params: data
  })
}

/**
 * 修改密码
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updatePasswordAPI(data) {
  return request({
    url: '/sys/user/updatePassword',
    method: 'post',
    params: data
  })
}