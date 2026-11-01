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
import uViewPlus, {setConfig} from "uview-plus";
import store from './store';

// uview 把输入类组件的文字色与占位符色写死成 JS 默认值内联下发，SCSS 变量重定向管不到，这里改成引用 CSS 变量
setConfig({
  props: {
    input: {
      color: 'var(--app-text-primary)',
      placeholderStyle: 'color: var(--app-text-placeholder)'
    },
    textarea: {
      placeholderStyle: 'color: var(--app-text-placeholder)'
    },
    search: {
      color: 'var(--app-text-primary)',
      placeholderColor: 'var(--app-text-placeholder)'
    },
    codeInput: {
      color: 'var(--app-text-primary)'
    }
  }
});

export function createApp() {
  const app = createSSRApp(App);
  app.use(uViewPlus);
  app.use(store);
  return {
    app
  }
}
