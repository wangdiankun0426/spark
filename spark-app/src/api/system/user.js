import request from '@/api/request'
import store from '@/store'

/**
 * 查询当前用户数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function userDetailAPI() {
  return request({
    url: '/system/user/detail',
    method: 'get',
  })
}

/**
 * 查询当前用户菜单
 * @returns {Promise<AxiosResponse<any>>}
 */
export function userMenuTreeAPI() {
  return request({
    url: '/system/user/menuTree',
    method: 'get',
  })
}

/**
 * 分页查询用户列表
 * @param query
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageUserListAPI(query) {
  return request({
    url: '/system/user/pageList',
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
    url: '/system/user/updatePassword',
    method: 'post',
    params: data
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
      url: baseUrl + '/system/user/upload/avatar',
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

