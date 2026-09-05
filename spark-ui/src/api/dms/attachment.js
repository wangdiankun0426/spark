import request from '@/api/request.js'

/**
 * 上传附件
 * @param file 文件对象
 * @returns {Promise<AxiosResponse<any>>}
 */
export function uploadAttachmentAPI(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/dms/attachment/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/**
 * 初始化分片上传会话
 * @param {Object} params 分片参数 { fileName, fileSize, totalChunks }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function initAttachmentUploadAPI(params) {
    return request({
        url: '/dms/attachment/upload/init',
        method: 'post',
        params
    })
}

/**
 * 上传单个分片
 * @param {Object} params 分片参数 { uploadId, chunkIndex }
 * @param {Blob} chunkFile 分片文件
 * @returns {Promise<AxiosResponse<any>>}
 */
export function uploadAttachmentChunkAPI(params, chunkFile) {
    const formData = new FormData()
    formData.append('file', chunkFile)
    return request({
        url: '/dms/attachment/upload/chunk',
        method: 'post',
        params,
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/**
 * 查询已上传的分片序号
 * @param {Object} params 查询参数 { uploadId }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryAttachmentChunksAPI(params) {
    return request({
        url: '/dms/attachment/upload/chunks',
        method: 'get',
        params
    })
}

/**
 * 合并分片并保存为系统附件
 * @param {Object} params 合并参数 { uploadId }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function mergeAttachmentUploadAPI(params) {
    return request({
        url: '/dms/attachment/upload/merge',
        method: 'post',
        params
    })
}

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
