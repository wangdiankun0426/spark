import request from '@/api/request'

// 创建工作流
export function createWorkflowAPI(data) {
    return request({ url: '/workflow/create', method: 'post', params: data })
}

// 分页查询工作流列表
export function pageWorkflowListAPI(query) {
    return request({ url: '/workflow/pageList', method: 'get', params: query })
}

// 修改工作流
export function updateWorkflowAPI(data) {
    return request({ url: '/workflow/update', method: 'post', params: data })
}

// 删除工作流
export function deleteWorkflowAPI(data) {
    return request({ url: '/workflow/delete', method: 'post', params: data })
}

// 查询工作流详情
export function queryWorkflowDetailAPI(query) {
    return request({ url: '/workflow/detail', method: 'get', params: query })
}
