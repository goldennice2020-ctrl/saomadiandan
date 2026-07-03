import api from './api'

/**
 * 使用手机号登录
 */
export function userLogin(data) {
  return api.post('/member/auth/mobile-login', data, { login: false })
}

/**
 * userAuthSession   
 */
export function userAuthSession(data) {
  return api.post('/member/auth/auth-session', data, { login: false })
}

/**
 * wechatAuth   
 */
export function wechatAuth(data) {
  return api.get('/member/auth/auth-wechat-login', data, { login: false })
}
