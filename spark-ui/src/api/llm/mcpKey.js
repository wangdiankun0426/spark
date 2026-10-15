import request from '@/api/request.js'

/**
 * 申请 MCP 密钥
 * @param data 参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createMcpKeyAPI(data) {
    return request({
        url: '/llm/mcpKey/create',
        method: 'post',
        params: data
    })
}

/**
 * 删除 MCP 密钥
 * @param data 参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteMcpKeyAPI(data) {
    return request({
        url: '/llm/mcpKey/delete',
        method: 'post',
        params: data
    })
}

/**
 * 查询当前用户的 MCP 密钥列表
 * @returns {Promise<AxiosResponse<any>>}
 */
export function myMcpKeyListAPI() {
    return request({
        url: '/llm/mcpKey/myList',
        method: 'get'
    })
}
