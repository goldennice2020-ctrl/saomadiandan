/*
 * @Author: Gaoxs
 * @Date: 2023-04-07 15:12:06
 * @LastEditors: Gaoxs
 * @Description:
 */
import util from '@/utils'

import App from './App'

import { createPinia } from 'pinia'

import { createSSRApp } from 'vue'

const disableGlobalLoading = () => {
  if (typeof uni === 'undefined' || uni.__disableGlobalLoading) return
  uni.__disableGlobalLoading = true
  uni.hideLoading()
  uni.showLoading = (options = {}) => {
    if (options?.success) options.success()
    if (options?.complete) options.complete()
  }
}

export function createApp() {
  disableGlobalLoading()
  const app = createSSRApp(App)
  app.use(util)
  app.use(createPinia())
  return {
    app,
  }
}
