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