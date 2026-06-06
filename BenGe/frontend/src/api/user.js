import request from './request';

// 用户登录
export function login(username, password) {
  return request.post(`/user/login?username=${username}&password=${password}`);
}

// 用户注册
export function register(username, password) {
  return request.post('/user/register', { username, password });
}

// 退出登录
export function logout() {
  return request.post('/user/logout');
}

