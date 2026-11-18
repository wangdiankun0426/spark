import request from '@/api/request.js'

/**
 * 分页查询厂商密钥列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageProviderKeyListAPI(query) {
  return request({
    url: '/llm/providerKey/pageList',
    method: 'get',
    params: query
  })
}

/**
 * 查询厂商密钥详情
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryProviderKeyDetailAPI(query) {
  return request({
    url: '/llm/providerKey/detail',
    method: 'get',
    params: query
  })
}

/**
 * 创建厂商密钥
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createProviderKeyAPI(data) {
  return request({
    url: '/llm/providerKey/create',
    method: 'post',
    data
  })
}

/**
 * 修改厂商密钥
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateProviderKeyAPI(data) {
  return request({
    url: '/llm/providerKey/update',
    method: 'post',
    data
  })
}

/**
 * 删除厂商密钥
 * @param query 删除参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteProviderKeyAPI(query) {
  return request({
    url: '/llm/providerKey/delete',
    method: 'post',
    params: query
  })
}
