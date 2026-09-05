import {
    initAttachmentUploadAPI,
    uploadAttachmentChunkAPI,
    queryAttachmentChunksAPI,
    mergeAttachmentUploadAPI,
    uploadAttachmentAPI
} from '@/api/dms/attachment.js'

// 默认分片阈值：1MB，不超过直接整包上传
const DEFAULT_THRESHOLD = 1024 * 1024
// 默认分片大小：1MB
const DEFAULT_CHUNK_SIZE = 1024 * 1024
// 默认分片并发数
const DEFAULT_CONCURRENCY = 3
// 单片默认最大重试次数
const DEFAULT_MAX_RETRY = 3

/**
 * 上传文件：不超过阈值直接整包上传，超过则分片上传（支持断点续传）
 * @param {File} file 待上传文件
 * @param {Object} options 上传选项
 * @param {number} options.threshold 分片阈值（字节），默认 1MB
 * @param {number} options.chunkSize 分片大小（字节），默认 1MB
 * @param {number} options.concurrency 分片并发数，默认 3
 * @param {number} options.maxRetry 单片最大重试次数，默认 3
 * @param {string} options.uploadId 续传的上传会话id
 * @param {Function} options.onStatus 状态文案回调，如"开始分片：共 N 片"
 * @param {Function} options.onProgress 进度回调，参数为 { percent, uploadedBytes, totalBytes, speed, uploadedChunks, totalChunks }
 * @param {Function} options.onUploadId 获取上传会话id的回调，用于断点续传
 * @returns {Promise<Object>} 附件信息
 */
export async function chunkUploadFile(file, options = {}) {
    const threshold = options.threshold || DEFAULT_THRESHOLD
    if (file.size <= threshold) {
        return directUpload(file, options)
    }
    return chunkUpload(file, options)
}

/**
 * 整包直传（不超过阈值的小文件）
 * @param {File} file 待上传文件
 * @param {Object} options 上传选项
 * @returns {Promise<Object>} 附件信息
 */
async function directUpload(file, options) {
    const onStatus = options.onStatus || (() => {})
    const onProgress = options.onProgress || (() => {})
    onStatus('文件未超过 1MB，直接上传中')
    const res = await uploadAttachmentAPI(file)
    if (res.code !== 200 || !res.data) {
        throw new Error(res.message || '文件上传失败')
    }
    onProgress({
        percent: 100,
        uploadedBytes: file.size,
        totalBytes: file.size,
        speed: '',
        uploadedChunks: 0,
        totalChunks: 0
    })
    return res.data
}

/**
 * 分片上传：初始化会话 -> 并发上传分片 -> 合并
 * @param {File} file 待上传文件
 * @param {Object} options 上传选项
 * @returns {Promise<Object>} 附件信息
 */
async function chunkUpload(file, options) {
    const chunkSize = options.chunkSize || DEFAULT_CHUNK_SIZE
    const concurrency = options.concurrency || DEFAULT_CONCURRENCY
    const maxRetry = options.maxRetry || DEFAULT_MAX_RETRY
    const onStatus = options.onStatus || (() => {})
    const onProgress = options.onProgress || (() => {})
    const totalChunks = Math.ceil(file.size / chunkSize)
    onStatus(`开始分片：共 ${totalChunks} 片，每片 ${formatFileSize(chunkSize)}`)

    // 已上传分片集合（断点续传时跳过）
    const uploadedSet = new Set()
    let uploadId = options.uploadId
    if (uploadId) {
        const chunksRes = await queryAttachmentChunksAPI({ uploadId })
        if (chunksRes.code === 200 && Array.isArray(chunksRes.data)) {
            chunksRes.data.forEach(chunkIndex => uploadedSet.add(chunkIndex))
        } else {
            // 会话已失效，改走新会话
            uploadId = undefined
        }
    }
    if (!uploadId) {
        const initRes = await initAttachmentUploadAPI({ fileName: file.name, fileSize: file.size, totalChunks })
        if (initRes.code !== 200 || !initRes.data) {
            throw new Error(initRes.message || '初始化分片上传失败')
        }
        uploadId = initRes.data
    }
    // 暴露 uploadId 给调用方，用于断点续传
    if (options.onUploadId) {
        options.onUploadId(uploadId)
    }

    let uploadedBytes = 0
    for (let i = 0; i < totalChunks; i++) {
        if (uploadedSet.has(i)) {
            uploadedBytes += chunkBytes(i, chunkSize, file.size)
        }
    }
    const startTime = Date.now()
    const reportProgress = () => {
        const elapsed = (Date.now() - startTime) / 1000
        const speed = elapsed > 0 ? uploadedBytes / elapsed : 0
        onProgress({
            percent: Math.floor(uploadedBytes / file.size * 100),
            uploadedBytes,
            totalBytes: file.size,
            speed: formatFileSize(speed) + '/s',
            uploadedChunks: uploadedSet.size,
            totalChunks
        })
    }

    const uploadChunkWithRetry = async (chunkIndex) => {
        const start = chunkIndex * chunkSize
        const end = Math.min(start + chunkSize, file.size)
        const chunkFile = file.slice(start, end)
        for (let attempt = 0; attempt <= maxRetry; attempt++) {
            try {
                const res = await uploadAttachmentChunkAPI({ uploadId, chunkIndex }, chunkFile)
                if (res.code === 200) {
                    uploadedSet.add(chunkIndex)
                    uploadedBytes += end - start
                    onStatus(`分片上传中 ${uploadedSet.size}/${totalChunks}`)
                    reportProgress()
                    return
                }
                throw new Error(res.message || '分片上传失败')
            } catch (err) {
                if (attempt === maxRetry) {
                    // 记录会话id供断点续传使用
                    err.uploadId = uploadId
                    throw err
                }
            }
        }
    }

    const pendingList = []
    for (let i = 0; i < totalChunks; i++) {
        if (!uploadedSet.has(i)) {
            pendingList.push(i)
        }
    }
    let nextIndex = 0
    const worker = async () => {
        while (nextIndex < pendingList.length) {
            const chunkIndex = pendingList[nextIndex]
            nextIndex++
            await uploadChunkWithRetry(chunkIndex)
        }
    }
    const workerList = []
    const workerCount = Math.min(concurrency, pendingList.length)
    for (let i = 0; i < workerCount; i++) {
        workerList.push(worker())
    }
    await Promise.all(workerList)

    onStatus('分片上传完成，服务器合并中')
    const mergeRes = await mergeAttachmentUploadAPI({ uploadId })
    if (mergeRes.code !== 200 || !mergeRes.data) {
        throw new Error(mergeRes.message || '分片合并失败')
    }
    reportProgress()
    return mergeRes.data
}

/**
 * 计算指定分片的字节数
 * @param {number} chunkIndex 分片序号
 * @param {number} chunkSize 分片大小
 * @param {number} fileSize 文件总字节数
 * @returns {number} 分片字节数
 */
function chunkBytes(chunkIndex, chunkSize, fileSize) {
    const start = chunkIndex * chunkSize
    const end = Math.min(start + chunkSize, fileSize)
    return end - start
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化大小
 */
function formatFileSize(bytes) {
    if (!bytes || bytes <= 0) {
        return '0 B'
    }
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}
