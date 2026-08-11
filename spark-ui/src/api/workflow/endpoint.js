import request from '@/api/request'

// 创建端点
export function createEndpointAPI(data) {
    return request({ url: '/workflow/endpoint/create', method: 'post', params: data })
}

// 修改端点
export function updateEndpointAPI(data) {
    return request({ url: '/workflow/endpoint/update', method: 'post', params: data })
}

// 删除端点
export function deleteEndpointAPI(data) {
    return request({ url: '/workflow/endpoint/delete', method: 'post', params: data })
}

// 查询端点详情
export function queryEndpointAPI(query) {
    return request({ url: '/workflow/endpoint/detail', method: 'get', params: query })
}

// 按工作流查询端点列表
export function queryEndpointListAPI(query) {
    return request({ url: '/workflow/endpoint/list', method: 'get', params: query })
}
