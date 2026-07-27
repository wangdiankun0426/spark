import request from '@/api/request.js'

// 创建厂商
export function createProviderAPI(data) {
    return request({
        url: '/llm/provider/create',
        method: 'post',
        params: data
    })
}

// 分页查询厂商
export function pageProviderListAPI(query) {
    return request({
        url: '/llm/provider/pageList',
        method: 'get',
        params: query
    })
}

// 修改厂商
export function updateProviderAPI(data) {
    return request({
        url: '/llm/provider/update',
        method: 'post',
        params: data
    })
}

// 删除厂商
export function deleteProviderAPI(data) {
    return request({
        url: '/llm/provider/delete',
        method: 'post',
        params: data
    })
}

// 查询厂商详情
export function queryProviderDetailAPI(query) {
    return request({
        url: '/llm/provider/detail',
        method: 'get',
        params: query
    })
}
