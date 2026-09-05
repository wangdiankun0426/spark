import request from '@/api/request.js'

// 分页查询文档
export function pageDocumentListAPI(query) {
    return request({
        url: '/dms/document/pageList',
        method: 'get',
        params: query
    })
}

// 修改文档
export function updateDocumentAPI(data) {
    return request({
        url: '/dms/document/update',
        method: 'post',
        params: data
    })
}

// 删除文档
export function deleteDocumentAPI(data) {
    return request({
        url: '/dms/document/delete',
        method: 'post',
        params: data
    })
}

// 查询文档详情
export function queryDocumentDetailAPI(query) {
    return request({
        url: '/dms/document/detail',
        method: 'get',
        params: query
    })
}

// 检索文档
export function searchDocumentAPI(query) {
    return request({
        url: '/dms/document/search',
        method: 'get',
        params: query
    })
}

/**
 * 根据系统附件归档文档
 * @param {Object} params 归档参数 { attId, prtId }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function fileDocumentAPI(params) {
    return request({
        url: '/dms/document/fileDocument',
        method: 'post',
        params
    })
}

/**
 * 下载文档（二进制流）
 * @param {Object} query 查询条件 { id, ext }
 * @returns {Promise<Blob>}
 */
export function downloadDocumentAPI(query) {
    return request({
        url: '/dms/document/download',
        method: 'get',
        params: query,
        responseType: 'blob'
    })
}

/**
 * 批量删除文档
 * @param {Array} ids 文档ID列表
 * @returns {Promise}
 */
export function batchDeleteDocumentAPI(ids) {
    return request({
        url: '/dms/document/batchDelete',
        method: 'post',
        params: { ids }
    })
}

/**
 * 批量重新处理文档
 * @param {Array} ids 文档ID列表
 * @returns {Promise}
 */
export function batchReprocessDocumentAPI(ids) {
    return request({
        url: '/dms/document/batchReprocess',
        method: 'post',
        params: { ids }
    })
}

