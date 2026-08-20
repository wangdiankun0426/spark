import request from '@/api/request'

/**
 * 上传附件
 * @param file 文件对象
 * @returns {Promise<AxiosResponse<any>>}
 */
export function uploadSystemAttachmentAPI(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/system/attachment/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/**
 * 查询系统附件详情
 * @param {Object} query 查询条件 { id }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryAttachmentDetailAPI(query) {
    return request({
        url: '/system/attachment/detail',
        method: 'get',
        params: query
    })
}

/**
 * 下载系统附件（二进制流）
 * @param {Object} query 查询条件 { id }
 * @returns {Promise<Blob>}
 */
export function downloadSystemAttachmentAPI(query) {
    return request({
        url: '/system/attachment/download',
        method: 'get',
        params: query,
        responseType: 'blob'
    })
}
