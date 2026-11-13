import request from '@/api/request.js'

// 查询菜单列表
export function listMenuAPI() {
    return request({
        url: '/sys/menu/list',
        method: 'get'
    })
}

// 查询菜单树
export function treeMenuAPI() {
    return request({
        url: '/sys/menu/tree',
        method: 'get'
    })
}

// 查询菜单详情
export function detailMenuAPI(query) {
    return request({
        url: '/sys/menu/detail',
        method: 'get',
        params: query
    })
}

// 创建菜单
export function createMenuAPI(data) {
    return request({
        url: '/sys/menu/create',
        method: 'post',
        params: data
    })
}

// 修改菜单
export function updateMenuAPI(data) {
    return request({
        url: '/sys/menu/update',
        method: 'post',
        params: data
    })
}

// 删除菜单
export function deleteMenuAPI(data) {
    return request({
        url: '/sys/menu/delete',
        method: 'post',
        params: data
    })
}

// 查询角色授权菜单树
export function roleMenuTreeAPI() {
    return request({
        url: '/sys/menu/roleTree',
        method: 'get'
    })
}

// 查询组织菜单授权树
export function orgMenuTreeAPI() {
    return request({
        url: '/sys/menu/orgTree',
        method: 'get'
    })
}
