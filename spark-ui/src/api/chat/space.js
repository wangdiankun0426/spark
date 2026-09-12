import request from '@/api/request'

/**
 * 创建聊天空间
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createChatSpaceAPI(data) {
    return request({
        url: '/chat/space/create',
        method: 'post',
        params: data
    })
}

/**
 * 创建AI会话
 * @param data 会话参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createAiChatSpaceAPI(data) {
    return request({
        url: '/chat/space/aiCreate',
        method: 'post',
        params: data
    })
}

/**
 * 分页查询我的AI会话列表，支持按接收人与标题检索
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyChatSpaceListAPI(query) {
    return request({
        url: '/chat/space/pageMyList',
        method: 'get',
        params: query
    })
}

/**
 * 重命名我的AI会话
 * @param data 会话参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateChatSpaceTitleAPI(data) {
    return request({
        url: '/chat/space/rename',
        method: 'post',
        params: data
    })
}

/**
 * 删除我的AI会话
 * @param data 会话参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteChatSpaceAPI(data) {
    return request({
        url: '/chat/space/delete',
        method: 'post',
        params: data
    })
}
