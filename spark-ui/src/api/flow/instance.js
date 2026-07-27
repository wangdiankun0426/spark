import request from '@/api/request'

// 创建流程实例
export function createFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/create',
        method: 'post',
        data
    })
}

// 分页查询我申请的流程实例
export function pageMyAppliedListAPI(query) {
    return request({
        url: '/flow/instance/pageMyAppliedList',
        method: 'get',
        params: query
    })
}

// 分页查询我的待办流程实例
export function pageMyPendingListAPI(query) {
    return request({
        url: '/flow/instance/pageMyPendingList',
        method: 'get',
        params: query
    })
}

// 分页查询我的已办流程实例
export function pageMyPendedListAPI(query) {
    return request({
        url: '/flow/instance/pageMyPendedList',
        method: 'get',
        params: query
    })
}

// 分页查询流程实例详情
export function showInstanceDetailAPI(query) {
    return request({
        url: '/flow/instance/showDetail',
        method: 'get',
        params: query
    })
}

// 审批流程实例
export function approvalFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/approval',
        method: 'post',
        params: data
    })
}
