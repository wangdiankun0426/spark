// 微信小程序环境 polyfill：vendor 中可能检查 WebSocket in window/global
// #ifdef MP-WEIXIN
if (typeof global === 'undefined') {
  global = {};
}
if (typeof window === 'undefined') {
  window = global;
}
// #endif

import { createSSRApp } from 'vue';
import App from './App.vue'
import '@/api/request';
import uViewPlus from "uview-plus";
import store from './store';

export function createApp() {
  const app = createSSRApp(App);
  app.use(uViewPlus);
  app.use(store);
  return {
    app
  }
}
