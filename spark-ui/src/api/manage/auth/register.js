import request from '@/api/request.js'

/**
 * 用户注册
 * @param data 注册参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function registerAPI(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        params: data
    })
}
