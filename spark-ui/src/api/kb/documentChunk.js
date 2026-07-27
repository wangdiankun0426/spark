import request from "@/api/request";

// 分页查询文档分块
export function pageDocumentChunkListAPI(query) {
    return request({
        url: '/kb/document/chunk/pageList',
        method: 'get',
        params: query
    })
}

// 查询文档分块QA
export function pageDocumentChunkQAListAPI(query) {
    return request({
        url: '/kb/document/chunk/pageQAList',
        method: 'get',
        params: query
    })
}