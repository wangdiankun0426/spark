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
export function pageMyApplicationListAPI(query) {
    return request({
        url: '/flow/instance/pageMyApplicationList',
        method: 'get',
        params: query
    })
}

// 分页查询我的待办流程实例
export function pageMyTodoListAPI(query) {
    return request({
        url: '/flow/instance/pageMyTodoList',
        method: 'get',
        params: query
    })
}

// 分页查询我的已办流程实例
export function pageMyDoneList(query) {
    return request({
        url: '/flow/instance/pageMyDoneList',
        method: 'get',
        params: query
    })
}

// 分页查询全部流程实例
export function pageInstanceListAPI(query) {
    return request({
        url: '/flow/instance/pageList',
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

// 管理员干预审批流程实例
export function adminApprovalFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/adminApproval',
        method: 'post',
        data
    })
}

// 催办流程实例
export function urgeFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/urge',
        method: 'post',
        params: data
    })
}

// 转办流程实例
export function transferFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/transfer',
        method: 'post',
        params: data
    })
}

// 加签流程实例
export function addSignFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/addSign',
        method: 'post',
        params: data
    })
}

// 撤回流程实例
export function recallFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/recall',
        method: 'post',
        data
    })
}

// 抄送流程实例
export function copyFlowInstanceAPI(data) {
    return request({
        url: '/flow/instance/copy',
        method: 'post',
        data
    })
}

// 分页查询抄送给我列表
export function pageCopyMyListAPI(query) {
    return request({
        url: '/flow/instance/pageCopyMyList',
        method: 'get',
        params: query
    })
}
