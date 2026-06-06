import axios from 'axios';

// 创建axios实例
const request = axios.create({
  // 使用相对路径，让Vue代理处理
  baseURL: '/api',
  timeout: 10000 * 30, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage中获取token
    const token = localStorage.getItem('token');
    // 如果token存在，将其添加到请求头中
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    // console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 返回完整的响应数据
    return response.data;
  },
  error => {
    // 统一处理错误
    console.error('响应错误:', error);
    // 可以根据错误状态码做不同处理
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权处理（如重定向到登录页）
          console.error('未授权，请重新登录');
          // 清除token
          localStorage.removeItem('token');
          break;
        case 403:
          console.error('拒绝访问');
          break;
        case 404:
          console.error('请求的资源不存在');
          break;
        case 500:
          console.error('服务器错误');
          break;
        default:
          console.error(`未知错误: ${error.response.status}`);
      }
    } else {
      console.error('网络错误，请检查您的网络连接');
    }
    return Promise.reject(error);
  }
);

export default request; 