import request from '@/api/request'

// 创建表单
export function createFormAPI(data) {
    return request({
        url: '/form/create',
        method: 'post',
        params: data
    })
}

// 分页查询表单
export function pageFormListAPI(query) {
    return request({
        url: '/form/pageList',
        method: 'get',
        params: query
    })
}

// 查询表单列表
export function queryFormListAPI(query) {
    return request({
        url: '/form/list',
        method: 'get',
        params: query
    })
}

// 修改表单
export function updateFormAPI(data) {
    return request({
        url: '/form/update',
        method: 'post',
        params: data
    })
}

// 删除表单
export function deleteFormAPI(data) {
    return request({
        url: '/form/delete',
        method: 'post',
        params: data
    })
}

// 查询表单详情
export function queryFormDetailAPI(query) {
    return request({
        url: '/form/detail',
        method: 'get',
        params: query
    })
}

// 查询表单json
export function queryFormJsonAPI(query) {
    return request({
        url: '/form/queryJson',
        method: 'get',
        params: query
    })
}

// 保存表单json
export function saveFormJsonAPI(data) {
    return request({
        url: '/form/saveJson',
        method: 'post',
        data
    })
}
