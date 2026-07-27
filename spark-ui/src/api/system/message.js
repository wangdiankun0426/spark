import request from '@/api/request'

// 查询我的消息列表
export function queryMyMessageListAPI(query) {
    return request({
        url: '/system/message/myList',
        method: 'get',
        params: query
    })
}
