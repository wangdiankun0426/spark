import request from '@/api/request'

/**
 * 查询智能体列表
 * @param params
 * @returns {Promise<AxiosResponse<any>>}
 */
export function agentPageListAPI(params) {
  return request({
    url: '/llm/agent/pageList',
    method: 'get',
    params: params
  })
}
