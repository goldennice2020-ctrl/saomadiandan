import api from './api'

/**
 * 根据店铺 ID 获取店铺信息
 */
export function shopGet(data) {
  return api.get('/store/getShop', data, { login: false })
}
/**
 * 获取首页信息
 */
export function menuGoods(data) {
  return api.get('/product/products', data, { login: false })
}

/**
 * 解析微信小程序桌码
 */
export function resolveTableCode(data) {
  return api.get('/store/table-code/resolve', data, { login: false })
}
