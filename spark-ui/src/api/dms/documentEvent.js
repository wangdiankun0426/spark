import request from '@/api/request.js'

// 查询文档事件详情
export function queryDocumentEventDetailAPI(query) {
    return request({
        url: '/dms/document/event/detail',
        method: 'get',
        params: query
    })
}

// 修改文档事件（用于重置索引事件 / 向量化事件）
export function updateDocumentEventAPI(data) {
    return request({
        url: '/dms/document/event/update',
        method: 'post',
        params: data
    })
}