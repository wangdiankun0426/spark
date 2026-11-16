import request from '@/api/request.js'

/**
 * 创建租户
 * @param data 租户表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createTenantAPI(data) {
    return request({
        url: '/sys/tenant/create',
        method: 'post',
        params: data
    })
}

/**
 * 分页查询租户列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageTenantListAPI(query) {
    return request({
        url: '/sys/tenant/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 修改租户
 * @param data 租户表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateTenantAPI(data) {
    return request({
        url: '/sys/tenant/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除租户
 * @param data 租户表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteTenantAPI(data) {
    return request({
        url: '/sys/tenant/delete',
        method: 'post',
        params: data
    })
}

/**
 * 查询租户已授权的菜单id集合
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryTenantMenuIdsAPI(query) {
    return request({
        url: '/sys/tenant/menuIds',
        method: 'get',
        params: query
    })
}

/**
 * 保存租户菜单授权
 * @param data 授权参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function saveTenantMenusAPI(data) {
    return request({
        url: '/sys/tenant/saveMenu',
        method: 'post',
        params: data
    })
}
