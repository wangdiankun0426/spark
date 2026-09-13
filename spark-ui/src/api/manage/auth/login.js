import request from '@/api/request.js'

/**
 * 登录
 * @param data
 */
export function loginAPI(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        params: data
    })
}

/**
 * 查询一次性加密密钥
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getEncryptKeyAPI() {
    return request({
        url: '/auth/encryptKey',
        method: 'post'
    })
}

/**
 * 登出
 */
export function logoutAPI() {
    return request({
        url: '/auth/logout',
        method: 'post'
    })
}

/**
 * 查询图形验证码
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getValidateCodeAPI() {
    return request({
        url: '/auth/validateCode',
        method: 'get',
    })
}

/**
 * 查询短信验证码
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getSmsCodeAPI(param) {
    return request({
        url: '/auth/smsCode',
        method: 'get',
        params: param
    })
}

/**
 * 查询邮箱验证码
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getEmailCodeAPI(param) {
    return request({
        url: '/auth/emailCode',
        method: 'get',
        params: param
    })
}


/**
 * 强制退出
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function forceLogoutAPI(data)  {
    return request({
        url: '/auth/forceLogout',
        method: 'post',
        params: data
    })
}

/**
 * 查询session
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getSessionAPI() {
    return request({
        url: '/auth/session',
        method: 'get'
    })
}