import request from '@/api/request.js'

/**
 * 查询我的消息列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyMessageListAPI(query) {
    return request({
        url: '/sys/message/pageMyList',
        method: 'get',
        params: query
    })
}

/**
 * 查询我的消息分类计数，覆盖全部、未读、已读
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryMyMessageCountAPI() {
    return request({
        url: '/sys/message/myCount',
        method: 'get'
    })
}

/**
 * 标记消息已读，已读状态在服务端持久化，重复调用幂等
 * @param data 消息参数，至少含 id
 * @returns {Promise<AxiosResponse<any>>}
 */
export function readMessageAPI(data) {
    return request({
        url: '/sys/message/read',
        method: 'post',
        params: data
    })
}

/**
 * 分页查询消息列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMessageListAPI(query) {
    return request({
        url: '/sys/message/pageList',
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
        url: '/sys/message/send',
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
        url: '/sys/message/delete',
        method: 'post',
        params: data
    })
}
