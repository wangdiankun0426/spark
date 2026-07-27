import request from '@/api/request'

// 添加用户
export function addUserAPI(data) {
    return request({
        url: '/system/role/user/add',
        method: 'post',
        params: data
    })
}

// 查询已添加用户列表
export function pageRoleUserListAPI(query) {
    return request({
        url: '/system/role/user/pageList',
        method: 'get',
        params: query
    })
}

// 移除用户
export function removeUserAPI(data) {
    return request({
        url: '/system/role/user/remove',
        method: 'post',
        params: data
    })
}
