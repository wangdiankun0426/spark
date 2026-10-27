import request from '@/api/request.js'

/**
 * 分页查询模型用量明细
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageModelUsageListAPI(query) {
    return request({
        url: '/llm/modelUsage/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 按模型类型汇总用量
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryModelUsageSummaryAPI(query) {
    return request({
        url: '/llm/modelUsage/summary',
        method: 'get',
        params: query
    })
}

/**
 * 按日期与类型查询用量趋势
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryModelUsageTrendAPI(query) {
    return request({
        url: '/llm/modelUsage/trend',
        method: 'get',
        params: query
    })
}
