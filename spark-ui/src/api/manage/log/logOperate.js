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
