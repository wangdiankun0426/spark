import request from '@/api/request'

/**
 * 分页查询知识图谱列表
 * @param {Object} query 查询条件 { name, status, pageNo, pageSize }
 * @returns {Promise}
 */
export function pageGraphListAPI(query) {
    return request({
        url: '/kg/graph/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 查询知识图谱详情
 * @param {Object} query { id }
 * @returns {Promise}
 */
export function queryGraphDetailAPI(query) {
    return request({
        url: '/kg/graph/detail',
        method: 'get',
        params: query
    })
}

/**
 * 创建知识图谱
 * @param {Object} data 知识图谱信息
 * @returns {Promise}
 */
export function createGraphAPI(data) {
    return request({
        url: '/kg/graph/create',
        method: 'post',
        params: data
    })
}

/**
 * 修改知识图谱
 * @param {Object} data 知识图谱信息
 * @returns {Promise}
 */
export function updateGraphAPI(data) {
    return request({
        url: '/kg/graph/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除知识图谱
 * @param {Object} data { id }
 * @returns {Promise}
 */
export function deleteGraphAPI(data) {
    return request({
        url: '/kg/graph/delete',
        method: 'post',
        params: data
    })
}
