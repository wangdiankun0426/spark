import request from '@/api/request.js'

/**
 * 分页查询日志列表
 * @param query
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageLogOperateListAPI(query)  {
    return request({
        url: '/log/operate/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 查询日志操作类型列表
 * @param query
 * @returns {Promise<AxiosResponse<any>>}
 */
export function logOperateTypeListAPI(query)  {
    return request({
        url: '/log/operate/typeList',
        method: 'get',
        params: query
    })
}

/**
 * 查询操作日志统计
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getOperateStatisticsAPI() {
    return request({
        url: '/log/operate/statistics',
        method: 'get'
    })
}

/**
 * 查询对象操作记录列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryObjOperateLogListAPI(query) {
    return request({
        url: '/log/operate/objList',
        method: 'get',
        params: query
    })
}
