import request from '@/api/request'

/**
 * 查询我的消息列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryMyMessageListAPI(query) {
    return request({
        url: '/system/message/myList',
        method: 'get',
        params: query
    })
}

/**
 * 分页查询消息列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMessageListAPI(query) {
    return request({
        url: '/system/message/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 发送消息
 * @param data 消息参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function sendMessageAPI(data) {
    return request({
        url: '/system/message/send',
        method: 'post',
        params: data
    })
}

/**
 * 删除消息
 * @param data 删除参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteMessageAPI(data) {
    return request({
        url: '/system/message/delete',
        method: 'post',
        params: data
    })
}
