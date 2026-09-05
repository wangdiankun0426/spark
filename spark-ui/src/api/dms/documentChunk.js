import request from "@/api/request.js";

// 分页查询文档分块
export function pageDocumentChunkListAPI(query) {
    return request({
        url: '/dms/document/chunk/pageList',
        method: 'get',
        params: query
    })
}

// 查询文档分块QA
export function pageDocumentChunkQAListAPI(query) {
    return request({
        url: '/dms/document/chunk/pageQAList',
        method: 'get',
        params: query
    })
}

// 查询文档分块统计信息
export function getDocumentChunkStatsAPI(docId) {
    return request({
        url: '/dms/document/chunk/stats',
        method: 'get',
        params: { docId }
    })
}

// 重新分块文档
export function rechunkDocumentAPI(docId) {
    return request({
        url: '/dms/document/chunk/rechunk',
        method: 'get',
        params: { docId }
    })
}

// 编辑分块内容
export function updateChunkAPI(data) {
    return request({
        url: '/dms/document/chunk/update',
        method: 'post',
        params: data
    })
}