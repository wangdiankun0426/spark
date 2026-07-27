import axios from 'axios'
// 由于微信小程序和app 用并没有xmlhttpRequest 对象,需要设置对应的适配器,
// 实际上就是调用了 uni.request 方法
import {
    UniAdapter
} from "uniapp-axios-adapter";
import {toast} from "uview-plus";
import store from '@/store'

const service = axios.create({
    baseURL: process.env.BASE_HTTP_API,
    timeout: 1000 * 60,
    adapter: UniAdapter,
})

//真机获取 解决app上adapter is not a function问题
axios.defaults.adapter = function(config) {
    return new Promise((resolve, reject) => {
        var settle = require('axios/lib/core/settle');
        var buildURL = require('axios/lib/helpers/buildURL');
        var buildFullPath = require('axios/lib/core/buildFullPath');
        let fullurl = buildFullPath(config.baseURL,config.url)
        uni.request({
            method: config.method.toUpperCase(),
            url: buildURL(fullurl, config.params, config.paramsSerializer),
            header: config.headers,
            data: config.data,
            dataType: config.dataType,
            responseType: config.responseType,
            sslVerify: config.sslVerify,
            complete:function complete(response){
                response = {
                    data: response.data,
                    status: response.statusCode,
                    errMsg: response.errMsg,
                    header: response.header,
                    config: config
                };

                settle(resolve, reject, response);
            }
        })
    })
}

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 添加请求头等前置处理
        config.headers['Authorization'] = store.getters['user/getToken'];
        // 如果存在params，则处理它
        if (config.params) {
            // 使用Object.keys遍历对象属性并过滤掉undefined和null的值
            config.params = Object.keys(config.params)
                .filter(key => config.params[key] !== undefined && config.params[key] !== null)
                .reduce((obj, key) => {
                    obj[key] = config.params[key];
                    return obj;
                }, {});
        }
        return config
    },
    error => {
        // 请求错误处理
        toast(error);
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        // 响应后处理
        if (response.status === 200 && response.data.code === 200) {
            return Promise.resolve(response.data)
        } else {
            if (response.data.code === 601) {
                uni.reLaunch({
                    url: '/pages/login'
                });
            } else {
                toast(response.data.message);
                return Promise.resolve(response.data)
            }
        }
    },
    error => {
        toast(error);
        return Promise.reject(error)
    }
)

export default service
