import request from '@/api/request'

// 分页查询表单
export function queryFormFieldListAPI(query) {
    return request({
        url: '/form/field/list',
        method: 'get',
        params: query
    })
}