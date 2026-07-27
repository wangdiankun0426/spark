import request from '@/api/request'

// 分页查询文档
export function pageDocumentListAPI(query) {
    return request({
        url: '/kb/document/pageList',
        method: 'get',
        params: query
    })
}

// 修改文档
export function updateDocumentAPI(data) {
    return request({
        url: '/kb/document/update',
        method: 'post',
        params: data
    })
}

// 删除文档
export function deleteDocumentAPI(data) {
    return request({
        url: '/kb/document/delete',
        method: 'post',
        params: data
    })
}

// 查询文档详情
export function queryDocumentDetailAPI(query) {
    return request({
        url: '/kb/document/detail',
        method: 'get',
        params: query
    })
}

// 检索文档
export function searchDocumentAPI(query) {
    return request({
        url: '/kb/document/search',
        method: 'get',
        params: query
    })
}

/**
 * 下载文档（二进制流）
 * @param {Object} query 查询条件 { id, ext }
 * @returns {Promise<Blob>}
 */
export function downloadDocumentAPI(query) {
    return request({
        url: '/kb/document/download',
        method: 'get',
        params: query,
        responseType: 'blob'
    })
}

