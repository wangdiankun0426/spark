import request from '@/api/request'

/**
 * 分页查询实体列表
 * @param {Object} query 查询条件 { graphId, name, type, status, pageNo, pageSize }
 * @returns {Promise}
 */
export function pageEntityListAPI(query) {
    return request({
        url: '/kg/entity/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 查询实体详情
 * @param {Object} query { id }
 * @returns {Promise}
 */
export function queryEntityDetailAPI(query) {
    return request({
        url: '/kg/entity/detail',
        method: 'get',
        params: query
    })
}

/**
 * 创建实体
 * @param {Object} data 实体信息
 * @returns {Promise}
 */
export function createEntityAPI(data) {
    return request({
        url: '/kg/entity/create',
        method: 'post',
        params: data
    })
}

/**
 * 修改实体
 * @param {Object} data 实体信息
 * @returns {Promise}
 */
export function updateEntityAPI(data) {
    return request({
        url: '/kg/entity/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除实体
 * @param {Object} data { id }
 * @returns {Promise}
 */
export function deleteEntityAPI(data) {
    return request({
        url: '/kg/entity/delete',
        method: 'post',
        params: data
    })
}

/**
 * 审核实体
 * @param {Object} data { entityId, auditStatus }
 * @returns {Promise}
 */
export function auditEntityAPI(data) {
    return request({
        url: '/kg/entity/audit',
        method: 'post',
        params: data
    })
}

/**
 * 批量审核实体
 * @param {Object} data { entityIds, auditStatus }
 * @returns {Promise}
 */
export function batchAuditEntityAPI(data) {
    return request({
        url: '/kg/entity/batchAudit',
        method: 'post',
        params: data
    })
}
