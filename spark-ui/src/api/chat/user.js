import request from '@/api/request'

/**
 * 查询我的聊天用户列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getMyChatUserListAPI(query) {
    return request({
        url: '/chat/user/myList',
        method: 'get',
        params: query
    })
}
