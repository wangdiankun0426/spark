import request from '@/api/request.js'

/**
 * 创建租户配置
 * @param data 租户配置表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createTenantConfigAPI(data) {
    return request({
        url: '/sys/tenant/config/create',
        method: 'post',
        params: data
    })
}

/**
 * 分页查询租户配置列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageTenantConfigListAPI(query) {
    return request({
        url: '/sys/tenant/config/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 修改租户配置
 * @param data 租户配置表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateTenantConfigAPI(data) {
    return request({
        url: '/sys/tenant/config/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除租户配置
 * @param data 租户配置表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteTenantConfigAPI(data) {
    return request({
        url: '/sys/tenant/config/delete',
        method: 'post',
        params: data
    })
}

/**
 * 查询当前租户密码长度规则
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getPasswordRuleAPI() {
    return request({
        url: '/sys/tenant/config/passwordRule',
        method: 'get'
    })
}
