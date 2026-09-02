import request from '@/api/request'

// 创建技能
export function createSkillAPI(data) {
    return request({
        url: '/llm/skill/create',
        method: 'post',
        params: data
    })
}

// 分页查询技能
export function pageSkillListAPI(query) {
    return request({
        url: '/llm/skill/pageList',
        method: 'get',
        params: query
    })
}

// 修改技能
export function updateSkillAPI(data) {
    return request({
        url: '/llm/skill/update',
        method: 'post',
        params: data
    })
}

// 删除技能
export function deleteSkillAPI(data) {
    return request({
        url: '/llm/skill/delete',
        method: 'post',
        params: data
    })
}

// 查询技能详情
export function querySkillDetailAPI(query) {
    return request({
        url: '/llm/skill/detail',
        method: 'get',
        params: query
    })
}
