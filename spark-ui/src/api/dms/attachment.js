import request from '@/api/request.js'

/**
 * 查询系统附件详情
 * @param {Object} query 查询条件 { id }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryAttachmentDetailAPI(query) {
    return request({
        url: '/dms/attachment/detail',
        method: 'get',
        params: query
    })
}

/**
 * 下载系统附件（二进制流）
 * @param {Object} query 查询条件 { id }
 * @returns {Promise<Blob>}
 */
export function downloadAttachmentAPI(query) {
    return request({
        url: '/dms/attachment/download',
        method: 'get',
        params: query,
        responseType: 'blob'
    })
}
