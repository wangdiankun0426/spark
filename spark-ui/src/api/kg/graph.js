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

/**
 * 按需展开节点关联子图（可视化增量加载）
 * @param {Object} params { nodeId, depth }
 * @returns {Promise}
 */
export function expandNodeAPI(params) {
    return request({
        url: '/kg/graph/visual/expand',
        method: 'get',
        params: params
    })
}

/**
 * 查询图谱统计信息
 * @param {number} graphId 图谱 id
 * @returns {Promise}
 */
export function queryGraphStatsAPI(graphId) {
    return request({
        url: '/kg/graph/visual/stats',
        method: 'get',
        params: { graphId }
    })
}
