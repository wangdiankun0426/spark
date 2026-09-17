import request from '@/api/request.js'

/**
 * 回滚文档版本
 * @param {Object} data 版本参数 { id }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function rollbackDocumentVersionAPI(data) {
    return request({
        url: '/dms/document/version/rollback',
        method: 'post',
        params: data
    })
}

/**
 * 删除文档历史版本
 * @param {Object} data 版本参数 { id }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteDocumentVersionAPI(data) {
    return request({
        url: '/dms/document/version/delete',
        method: 'post',
        params: data
    })
}

/**
 * 分页查询文档版本
 * @param {Object} query 查询参数 { docId, pageNo, pageSize }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageDocumentVersionListAPI(query) {
    return request({
        url: '/dms/document/version/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 下载文档历史版本（二进制流）
 * @param {Object} query 查询条件 { id, ext }
 * @returns {Promise<Blob>}
 */
export function downloadDocumentVersionAPI(query) {
    return request({
        url: '/dms/document/version/download',
        method: 'get',
        params: query,
        responseType: 'blob'
    })
}
