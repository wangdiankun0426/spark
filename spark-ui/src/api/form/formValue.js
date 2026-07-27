import request from '@/api/request'

// 保存表单数据
export function saveFormValueAPI(data) {
    return request({
        url: '/form/value/save',
        method: 'post',
        data
    })
}

// 查询表单数据
export function detailFormValueAPI(query) {
    return request({
        url: '/form/value/detail',
        method: 'get',
        params: query
    })
}
