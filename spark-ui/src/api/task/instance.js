import request from '@/api/request'

/**
 * 分页查询任务实例列表（按业务对象id查询）
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageTaskInstanceListAPI(query) {
    return request({
        url: '/task/instance/pageList',
        method: 'get',
        params: query
    })
}
