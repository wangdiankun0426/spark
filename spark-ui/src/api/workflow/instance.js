import request from '@/api/request'

// 分页查询运行历史
export function pageInstanceHistoryAPI(query) {
    return request({ url: '/workflow/instance/pageList', method: 'get', params: query })
}

// 查询运行详情
export function queryInstanceDetailAPI(query) {
    return request({ url: '/workflow/instance/detail', method: 'get', params: query })
}

// 查询运行节点记录
export function queryInstanceNodesAPI(query) {
    return request({ url: '/workflow/instance/nodes', method: 'get', params: query })
}

// 快速运行工作流
export function executeWorkflowAPI(path, data) {
    return request({ url: '/workflow/instance/' + path, method: 'post', data: data })
}
