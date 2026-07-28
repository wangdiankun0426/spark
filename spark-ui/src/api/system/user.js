import request from '@/api/request'

/**
 * 查询用户详情
 * @returns {Promise<AxiosResponse<any>>}
 */
export function userDetailAPI(query) {
  return request({
    url: '/system/user/detail',
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
    url: '/system/user/pageList',
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
    url: '/system/user/create',
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
    url: '/system/user/update',
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
    url: '/system/user/delete',
    method: 'post',
    params: data
  })
}

/**
 * 强制退出
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function forceLogoutAPI(data)  {
  return request({
    url: '/system/user/forceLogout',
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
    url: '/system/user/updatePassword',
    method: 'post',
    params: data
  })
}

/**
 * 同步企业微信组织架构
 * @returns {Promise<AxiosResponse<any>>}
 */
export function syncWeComOrganizationAPI() {
  return request({
    url: '/weCom/syncOrganization',
    method: 'get'
  })
}
