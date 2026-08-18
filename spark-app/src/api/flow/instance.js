import request from '@/api/request'

/**
 * 分页查询我申请的流程实例
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyAppliedListAPI(params) {
    return request({
        url: '/flow/instance/pageMyAppliedList',
        method: 'get',
        params: params
    })
}

/**
 * 分页查询我的待办流程实例
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyPendingListAPI(params) {
    return request({
        url: '/flow/instance/pageMyPendingList',
        method: 'get',
        params: params
    })
}

/**
 * 分页查询我的已办流程实例
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyPendedListAPI(params) {
    return request({
        url: '/flow/instance/pageMyPendedList',
        method: 'get',
        params: params
    })
}

/**
 * 查询流程实例详情
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function showInstanceDetailAPI(params) {
    return request({
        url: '/flow/instance/showDetail',
        method: 'get',
        params: params
    })
}

/**
 * 审批流程实例（通过/驳回）
 * @param data 审批参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function approvalFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/approval',
        method: 'post',
        params: data
    })
}

/**
 * 催办流程实例
 * @param data 催办参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function urgeFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/urge',
        method: 'post',
        params: data
    })
}

/**
 * 转办流程实例
 * @param data 转办参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function transferFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/transfer',
        method: 'post',
        params: data
    })
}

/**
 * 加签流程实例
 * @param data 加签参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function addSignFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/addSign',
        method: 'post',
        params: data
    })
}
