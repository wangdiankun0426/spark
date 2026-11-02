import request from '@/api/request'

/**
 * 分页查询当前用户消息列表
 * @param params 查询参数，含 pageNo、pageSize 与 filterType，filterType 取 all、unread、read
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageMyMessageListAPI(params) {
  return request({
    url: '/sys/message/pageMyList',
    method: 'get',
    params: params
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
 * @param data 消息数据，至少含 id
 * @returns {Promise<AxiosResponse<any>>}
 */
export function readMessageAPI(data) {
  return request({
    url: '/sys/message/read',
    method: 'post',
    params: data
  })
}
