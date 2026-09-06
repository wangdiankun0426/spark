import request from "@/api/request.js";


/**
 * 同步企业微信组织架构
 * @returns {Promise<AxiosResponse<any>>}
 */
export function syncWeComOrganizationAPI() {
    return request({
        url: '/weCom/syncOrganization',
        method: 'get'
    })
}
