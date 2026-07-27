import request from '@/api/request'

// 分页查询表单版本
export function pageFormVersionListAPI(query) {
    return request({
        url: '/form/version/pageList',
        method: 'get',
        params: query
    })
}
