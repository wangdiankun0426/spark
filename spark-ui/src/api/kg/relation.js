import request from '@/api/request'

/**
 * 分页查询关系列表
 * @param {Object} query 查询条件 { graphId, headEntityId, tailEntityId, relationType, status, pageNo, pageSize }
 * @returns {Promise}
 */
export function pageRelationListAPI(query) {
    return request({
        url: '/kg/relation/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 查询关系详情
 * @param {Object} query { id }
 * @returns {Promise}
 */
export function queryRelationDetailAPI(query) {
    return request({
        url: '/kg/relation/detail',
        method: 'get',
        params: query
    })
}

/**
 * 创建关系
 * @param {Object} data 关系信息
 * @returns {Promise}
 */
export function createRelationAPI(data) {
    return request({
        url: '/kg/relation/create',
        method: 'post',
        params: data
    })
}

/**
 * 修改关系
 * @param {Object} data 关系信息
 * @returns {Promise}
 */
export function updateRelationAPI(data) {
    return request({
        url: '/kg/relation/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除关系
 * @param {Object} data { id }
 * @returns {Promise}
 */
export function deleteRelationAPI(data) {
    return request({
        url: '/kg/relation/delete',
        method: 'post',
        params: data
    })
}
