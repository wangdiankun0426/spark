import request from '@/api/request'

// 创建部门
export function createDeptAPI(data) {
    return request({
        url: '/sys/dept/create',
        method: 'post',
        params: data
    })
}

// 查询部门树
export function treeDeptAPI(query) {
    return request({
        url: '/sys/dept/tree',
        method: 'get',
        params: query
    })
}

// 修改部门
export function updateDeptAPI(data) {
    return request({
        url: '/sys/dept/update',
        method: 'post',
        params: data
    })
}

// 删除部门
export function deleteDeptAPI(data) {
    return request({
        url: '/sys/dept/delete',
        method: 'post',
        params: data
    })
}
