import request from '@/api/request.js'

/**
 * 查询 OnlyOffice 编辑器配置
 * @param {Object} query 查询条件 { id }
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryOnlyOfficeConfigAPI(query) {
    return request({
        url: '/dms/onlyoffice/config',
        method: 'get',
        params: query
    })
}
