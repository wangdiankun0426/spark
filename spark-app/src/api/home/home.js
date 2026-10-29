import request from '@/api/request'

/**
 * 查询我的资产数量
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryAssetAPI() {
    return request({
        url: '/home/asset',
        method: 'get'
    })
}

/**
 * 查询我的待办总数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryTodoTotalAPI() {
    return request({
        url: '/home/todoTotal',
        method: 'get'
    })
}

/**
 * 查询综合推荐榜
 * @param params 查询参数，rangeDays 统计天数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryRecommendAPI(params) {
    return request({
        url: '/home/recommend',
        method: 'get',
        params: params
    })
}
