import request from '@/api/request.js'

// 创建 MCP 服务器
export function createMcpAPI(data) {
    return request({
        url: '/llm/mcp/create',
        method: 'post',
        params: data
    })
}

// 修改 MCP 服务器
export function updateMcpAPI(data) {
    return request({
        url: '/llm/mcp/update',
        method: 'post',
        params: data
    })
}

// 删除 MCP 服务器
export function deleteMcpAPI(data) {
    return request({
        url: '/llm/mcp/delete',
        method: 'post',
        params: data
    })
}

// 分页查询 MCP 服务器列表
export function pageMcpListAPI(query) {
    return request({
        url: '/llm/mcp/pageList',
        method: 'get',
        params: query
    })
}

// 查询 MCP 服务器详情
export function detailMcpAPI(query) {
    return request({
        url: '/llm/mcp/detail',
        method: 'get',
        params: query
    })
}

// 测试 MCP 服务器连通性
export function testConnectionMcpAPI(data) {
    return request({
        url: '/llm/mcp/testConnection',
        method: 'post',
        params: data
    })
}
