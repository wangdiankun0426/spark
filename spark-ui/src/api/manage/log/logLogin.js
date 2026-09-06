import request from '@/api/request.js'

/**
 * 分页查询日志列表
 * @param query
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageLogLoginListAPI(query)  {
    return request({
        url: '/log/login/pageList',
        method: 'get',
        params: query
    })
}

