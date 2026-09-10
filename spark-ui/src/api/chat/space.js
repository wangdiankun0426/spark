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
 * 查询我的AI会话列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getMyAiChatSpaceListAPI(query) {
    return request({
        url: '/chat/space/aiMyList',
        method: 'get',
        params: query
    })
}

/**
 * 重命名我的AI会话
 * @param data 会话参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateAiChatSpaceTitleAPI(data) {
    return request({
        url: '/chat/space/aiRename',
        method: 'post',
        params: data
    })
}

/**
 * 删除我的AI会话
 * @param data 会话参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteAiChatSpaceAPI(data) {
    return request({
        url: '/chat/space/aiDelete',
        method: 'post',
        params: data
    })
}
