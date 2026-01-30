import axios from 'axios'

const request = axios.create({
    // ✅ 修改为相对路径，由 Nginx 接管转发
    baseURL: '/api', 
    timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    return config
}, error => {
    return Promise.reject(error)
});

// 响应拦截器
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果后端返回的是字符串，尝试转JSON
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        console.log('err' + error)
        return Promise.reject(error)
    }
)

export default request