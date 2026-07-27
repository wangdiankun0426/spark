import axios from 'axios'
import { ElMessage } from 'element-plus'
import store from "@/store/index.js";
import router from '@/router/index.js'

const service = axios.create({
    baseURL: process.env.BASE_HTTP_API,
    timeout: 1000 * 60
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 添加请求头等前置处理
        config.headers['Authorization'] = store.getters['user/getToken']
        // 如果存在params，则处理它
        if (config.params) {
            // 使用Object.keys遍历对象属性并过滤掉undefined、null和''的值
            config.params = Object.keys(config.params)
                .filter(key => config.params[key] !== undefined && config.params[key] !== null && config.params[key] !== '')
                .reduce((obj, key) => {
                    obj[key] = config.params[key];
                    return obj;
                }, {});
        }
        return config
    },
    error => {
        // 请求错误处理
        console.log('request error:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        // 二进制流响应（文件下载）直接返回 Blob
        if (response.config.responseType === 'blob' || response.data instanceof Blob) {
            return Promise.resolve(response.data)
        }
        // 响应后处理
        if (response.status === 200 && response.data.code === 200) {
            return Promise.resolve(response.data)
        } else {
            if (response.data.code === 601) {
                router.push('/login')
            } else {
                ElMessage.warning(response.data.message)
                return Promise.resolve(response.data)
            }
        }
    },
    error => {
        console.log('response error:', error)
        ElMessage.warning(error)
        return Promise.reject(error)
    }
)

export default service
