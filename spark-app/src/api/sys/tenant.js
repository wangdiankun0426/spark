import request from '@/api/request'

/**
 * 查询当前用户已加入且可用的租户列表
 * @returns {Promise}
 */
export function queryMyTenantListAPI() {
  return request({
    url: '/sys/tenant/user/myList',
    method: 'get'
  })
}

/**
 * 切换当前用户默认租户
 * @param data 租户id
 * @returns {Promise}
 */
export function switchTenantAPI(data) {
  return request({
    url: '/sys/tenant/user/switch',
    method: 'post',
    params: data
  })
}
