import request from '@/api/request.js'

// 查询菜单列表
export function listMenuAPI() {
    return request({
        url: '/sys/menu/list',
        method: 'get'
    })
}
