import request from '@/api/request'

/**
 * 查询未读消息列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getNoReadMsgListAPI(query) {
    return request({
        url: '/chat/msg/noReadList',
        method: 'get',
        params: query
    })
}

/**
 * 查询消息列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getMsgListAPI(query) {
    return request({
        url: '/chat/msg/list',
        method: 'get',
        params: query
    })
}

/**
 * 分页查询聊天记录列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyMsgListAPI(query) {
    return request({
        url: '/chat/msg/pageMyList',
        method: 'get',
        params: query
    })
}

/**
 * 分页查询消息列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMsgListAPI(query) {
    return request({
        url: '/chat/msg/pageList',
        method: 'get',
        params: query
    })
}