import request from '@/api/request'

// 创建流程模板版本
export function createTemplateVersionAPI(data) {
    return request({
        url: '/flow/template/version/create',
        method: 'post',
        data
    })
}

// 分页查询流程模板版本
export function pageTemplateVersionListAPI(query) {
    return request({
        url: '/flow/template/version/pageList',
        method: 'get',
        params: query
    })
}

// 修改流程模板版本
export function updateTemplateVersionAPI(data) {
    return request({
        url: '/flow/template/version/update',
        method: 'post',
        params: data
    })
}

// 删除流程模板版本
export function deleteTemplateVersionAPI(data) {
    return request({
        url: '/flow/template/version/delete',
        method: 'post',
        params: data
    })
}

// 查询流程模板版本详情
export function queryTemplateVersionDetailAPI(query) {
    return request({
        url: '/flow/template/version/detail',
        method: 'get',
        params: query
    })
}
