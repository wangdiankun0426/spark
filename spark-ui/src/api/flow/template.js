import request from '@/api/request'

// 创建流程模板
export function createTemplateAPI(data) {
    return request({
        url: '/flow/template/create',
        method: 'post',
        params: data
    })
}

// 分页查询流程模板
export function pageTemplateListAPI(query) {
    return request({
        url: '/flow/template/pageList',
        method: 'get',
        params: query
    })
}

// 修改流程模板
export function updateTemplateAPI(data) {
    return request({
        url: '/flow/template/update',
        method: 'post',
        params: data
    })
}

// 删除流程模板
export function deleteTemplateAPI(data) {
    return request({
        url: '/flow/template/delete',
        method: 'post',
        params: data
    })
}

// 查询流程模板详情
export function queryTemplateDetailAPI(query) {
    return request({
        url: '/flow/template/detail',
        method: 'get',
        params: query
    })
}

// 展示流程模板详情
export function showTemplateDetailAPI(query) {
    return request({
        url: '/flow/template/showDetail',
        method: 'get',
        params: query
    })
}