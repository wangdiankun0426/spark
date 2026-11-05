import request from '@/api/request'

/**
 * 查询综合推荐榜
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryRecommendAPI() {
    return request({
        url: '/home/recommend',
        method: 'get'
    })
}
