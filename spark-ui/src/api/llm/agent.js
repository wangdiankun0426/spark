import request from '@/api/request'

// 创建智能体
export function createAgentAPI(data) {
    return request({
        url: '/llm/agent/create',
        method: 'post',
        params: data
    })
}

// 分页查询智能体
export function pageAgentListAPI(query) {
    return request({
        url: '/llm/agent/pageList',
        method: 'get',
        params: query
    })
}

// 修改智能体
export function updateAgentAPI(data) {
    return request({
        url: '/llm/agent/update',
        method: 'post',
        params: data
    })
}

// 删除智能体
export function deleteAgentAPI(data) {
    return request({
        url: '/llm/agent/delete',
        method: 'post',
        params: data
    })
}

// 查询智能体详情
export function queryAgentDetailAPI(query) {
    return request({
        url: '/llm/agent/detail',
        method: 'get',
        params: query
    })
}
