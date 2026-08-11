import request from '@/api/request'

// 保存版本
export function saveVersionAPI(data) {
    return request({ url: '/workflow/version/saveVersion', method: 'post', data: data })
}

// 查询版本详情
export function queryVersionDetailAPI(query) {
    return request({ url: '/workflow/version/detail', method: 'get', params: query })
}
