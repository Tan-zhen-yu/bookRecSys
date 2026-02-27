import axios from 'axios'
// 在创建 axios 实例时或全局设置
axios.defaults.withCredentials = true;
const request = axios.create({
    // ✅ 修改为相对路径，由 Nginx 接管转发
    baseURL: '/api', 
    timeout: 5000
})

// 请求拦截器
// 请求拦截器
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

    // 1. 从 localStorage 拿到登录时保存的用户信息
    const userJson = localStorage.getItem("user");
    
    if (userJson) {
        const user = JSON.parse(userJson);
        // 2. 检查是否有 token，如果有，塞进 Headers
        // 注意：这里的键名 'token' 必须和 Java 拦截器里获取的键名一致
        if (user.token) {
            config.headers['token'] = user.token; 
        }
    }
    
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