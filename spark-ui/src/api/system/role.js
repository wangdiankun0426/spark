import request from '@/api/request'

// 创建角色
export function createRoleAPI(data) {
    return request({
        url: '/system/role/create',
        method: 'post',
        params: data
    })
}

// 分页查询角色列表
export function pageRoleListAPI(query) {
    return request({
        url: '/system/role/pageList',
        method: 'get',
        params: query
    })
}

// 修改角色
export function updateRoleAPI(data) {
    return request({
        url: '/system/role/update',
        method: 'post',
        params: data
    })
}

// 删除角色
export function deleteRoleAPI(data) {
    return request({
        url: '/system/role/delete',
        method: 'post',
        params: data
    })
}


