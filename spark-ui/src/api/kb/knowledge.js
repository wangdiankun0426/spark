import request from '@/api/request'

/**
 * 分页查询知识库列表
 * @param {Object} query 查询条件 { name, page, size }
 * @returns {Promise}
 */
export function pageKnowledgeListAPI(query) {
    return request({
        url: '/kb/knowledge/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 查询知识库详情
 * @param {Object} query { id }
 * @returns {Promise}
 */
export function queryKnowledgeDetailAPI(query) {
    return request({
        url: '/kb/knowledge/detail',
        method: 'get',
        params: query
    })
}

/**
 * 创建知识库
 * @param {Object} data 知识库信息
 * @returns {Promise}
 */
export function createKnowledgeAPI(data) {
    return request({
        url: '/kb/knowledge/create',
        method: 'post',
        params: data
    })
}

/**
 * 修改知识库
 * @param {Object} data 知识库信息
 * @returns {Promise}
 */
export function updateKnowledgeAPI(data) {
    return request({
        url: '/kb/knowledge/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除知识库
 * @param {Object} data { id }
 * @returns {Promise}
 */
export function deleteKnowledgeAPI(data) {
    return request({
        url: '/kb/knowledge/delete',
        method: 'post',
        params: data
    })
}
