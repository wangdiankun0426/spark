import request from '@/api/request'

/**
 * 分页查询任务模板列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageTaskTemplateListAPI(query) {
    return request({
        url: '/task/template/pageList',
        method: 'get',
        params: query
    })
}

/**
 * 创建任务模板
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createTaskTemplateAPI(data) {
    return request({
        url: '/task/template/create',
        method: 'post',
        params: data
    })
}

/**
 * 修改任务模板
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateTaskTemplateAPI(data) {
    return request({
        url: '/task/template/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除任务模板
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteTaskTemplateAPI(data) {
    return request({
        url: '/task/template/delete',
        method: 'post',
        params: data
    })
}

/**
 * 查询任务模板参数列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function listTaskTemplateParamAPI(query) {
    return request({
        url: '/task/template/param/list',
        method: 'get',
        params: query
    })
}

/**
 * 创建任务模板参数
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createTaskTemplateParamAPI(data) {
    return request({
        url: '/task/template/param/create',
        method: 'post',
        params: data
    })
}

/**
 * 修改任务模板参数
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updateTaskTemplateParamAPI(data) {
    return request({
        url: '/task/template/param/update',
        method: 'post',
        params: data
    })
}

/**
 * 删除任务模板参数
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function deleteTaskTemplateParamAPI(data) {
    return request({
        url: '/task/template/param/delete',
        method: 'post',
        params: data
    })
}
