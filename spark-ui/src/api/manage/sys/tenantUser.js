import request from '@/api/request.js'

/**
 * 添加用户到租户
 * @param data 添加参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function addTenantUserAPI(data) {
    return request({
        url: '/sys/tenant/user/add',
        method: 'post',
        params: data
    })
}

/**
 * 分页查询租户用户列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageTenantUserListAPI(query) {
    return request({
        url: '/sys/tenant/user/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 从租户移除用户
 * @param data 移除参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function removeTenantUserAPI(data) {
    return request({
        url: '/sys/tenant/user/remove',
        method: 'post',
        params: data
    })
}

/**
 * 修改租户用户角色类型
 * @param data 修改参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateTenantUserRoleTypeAPI(data) {
    return request({
        url: '/sys/tenant/user/updateRoleType',
        method: 'post',
        params: data
    })
}
