import request from '@/api/request'
import store from '@/store'

/**
 * 分页查询用户列表
 * @param query
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageUserListAPI(query) {
  return request({
    url: '/sys/user/pageList',
    method: 'get',
    params: query
  })
}

/**
 * 修改密码
 * @param data
 * @returns {Promise<AxiosResponse<any>>}
 */
export function updatePasswordAPI(data) {
  return request({
    url: '/sys/user/updatePassword',
    method: 'post',
    params: data
  })
}

/**
 * 查询当前租户密码长度规则
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getPasswordRuleAPI() {
  return request({
    url: '/sys/tenant/config/passwordRule',
    method: 'get'
  })
}

/**
 * 上传头像（使用 uni.uploadFile）
 * @param filePath 文件路径
 * @returns {Promise<any>}
 */
export function uploadAvatarAPI(filePath) {
  return new Promise((resolve, reject) => {
    const baseUrl = process.env.BASE_HTTP_API;
    const token = store.getters['user/getToken'] || '';
    uni.uploadFile({
      url: baseUrl + '/sys/user/upload/avatar',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token
      },
      success: (uploadFileRes) => {
        const data = JSON.parse(uploadFileRes.data);
        if (data.code === 200) {
          resolve(data);
        } else {
          reject(data);
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

