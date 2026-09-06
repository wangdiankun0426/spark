import request from '@/api/request.js'

// 创建角色
export function createRoleAPI(data) {
    return request({
        url: '/sys/role/create',
        method: 'post',
        params: data
    })
}

// 分页查询角色列表
export function pageRoleListAPI(query) {
    return request({
        url: '/sys/role/pageList',
        method: 'get',
        params: query
    })
}

// 修改角色
export function updateRoleAPI(data) {
    return request({
        url: '/sys/role/update',
        method: 'post',
        params: data
    })
}

// 删除角色
export function deleteRoleAPI(data) {
    return request({
        url: '/sys/role/delete',
        method: 'post',
        params: data
    })
}


