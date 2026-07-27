import request from '@/api/request.js'

// 创建模型
export function createModelAPI(data) {
    return request({
        url: '/llm/model/create',
        method: 'post',
        params: data
    })
}

// 分页查询模型
export function pageModelListAPI(query) {
    return request({
        url: '/llm/model/pageList',
        method: 'get',
        params: query
    })
}

// 修改模型
export function updateModelAPI(data) {
    return request({
        url: '/llm/model/update',
        method: 'post',
        params: data
    })
}

// 删除模型
export function deleteModelAPI(data) {
    return request({
        url: '/llm/model/delete',
        method: 'post',
        params: data
    })
}

// 查询模型详情
export function queryModelDetailAPI(query) {
    return request({
        url: '/llm/model/detail',
        method: 'get',
        params: query
    })
}
