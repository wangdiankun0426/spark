import request from '@/api/request'

/**
 * 分页查询我的聊天用户列表，带会话空间id与未读数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyChatUserListAPI(query) {
    return request({
        url: '/chat/user/pageMyList',
        method: 'get',
        params: query
    })
}
