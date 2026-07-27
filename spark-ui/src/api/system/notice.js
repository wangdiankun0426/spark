import request from '@/api/request'

// 创建公告
export function createNoticeAPI(data) {
    return request({
        url: '/system/notice/create',
        method: 'post',
        params: data
    })
}

// 分页查询
export function pageNoticeListAPI(query) {
    return request({
        url: '/system/notice/pageList',
        method: 'get',
        params: query
    })
}

// 修改公告
export function updateNoticeAPI(data) {
    return request({
        url: '/system/notice/update',
        method: 'post',
        params: data
    })
}

// 下架公告
export function delistNoticeAPI(data) {
    return request({
        url: '/system/notice/delist',
        method: 'post',
        params: data
    })
}

// 删除公告
export function deleteNoticeAPI(data) {
    return request({
        url: '/system/notice/delete',
        method: 'post',
        params: data
    })
}

// 查询公告详情
export function noticeDetailAPI(query) {
    return request({
        url: '/system/notice/detail',
        method: 'get',
        params: query
    })
}

// 保存公告内容
export function noticeSaveTextAPI(data) {
    return request({
        url: '/system/notice/saveText',
        method: 'post',
        data
    })
}

// 查询列表
export function noticeListAPI(query) {
    return request({
        url: '/system/notice/list',
        method: 'get',
        params: query
    })
}

// 查询公告内容
export function noticeViewTextAPI(query) {
    return request({
        url: '/system/notice/viewText',
        method: 'get',
        params: query
    })
}
