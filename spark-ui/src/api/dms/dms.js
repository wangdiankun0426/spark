import request from '@/api/request.js'

/**
 * 通用上传文件，携带 docId 生成文档新版本，携带 prtId 归档为文档，否则保存为附件
 * @param file 文件对象
 * @param {Object} params 上传参数 { prtId, docId }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function uploadFileAPI(file, params = {}) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/dms/upload',
        method: 'post',
        params,
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/**
 * 初始化分片上传会话
 * @param {Object} params 分片参数 { fileName, fileSize, totalChunks, prtId, docId }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function initUploadAPI(params) {
    return request({
        url: '/dms/upload/init',
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
export function uploadChunkAPI(params, chunkFile) {
    const formData = new FormData()
    formData.append('file', chunkFile)
    return request({
        url: '/dms/upload/chunk',
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
export function queryUploadChunksAPI(params) {
    return request({
        url: '/dms/upload/chunks',
        method: 'get',
        params
    })
}

/**
 * 合并分片，按会话携带的归档目标保存为文档或附件
 * @param {Object} params 合并参数 { uploadId }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function mergeUploadAPI(params) {
    return request({
        url: '/dms/upload/merge',
        method: 'post',
        params
    })
}
