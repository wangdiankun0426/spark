import request from '@/api/request'

/**
 * 分页查询我申请的流程实例
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyApplicationListAPI(params) {
    return request({
        url: '/flow/instance/pageMyApplicationList',
        method: 'get',
        params: params
    })
}

/**
 * 分页查询我的待办流程实例
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyTodoListAPI(params) {
    return request({
        url: '/flow/instance/pageMyTodoList',
        method: 'get',
        params: params
    })
}

/**
 * 分页查询我的已办流程实例
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyDoneListAPI(params) {
    return request({
        url: '/flow/instance/pageMyDoneList',
        method: 'get',
        params: params
    })
}

/**
 * 分页查询我的抄送列表
 * @param params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageCopyMyListAPI(params) {
    return request({
        url: '/flow/instance/pageCopyMyList',
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

/**
 * 撤回流程实例
 * @param data 撤回参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function recallFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/recall',
        method: 'post',
        data: data
    })
}

/**
 * 抄送流程实例
 * @param data 抄送参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function copyFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/copy',
        method: 'post',
        data: data
    })
}
