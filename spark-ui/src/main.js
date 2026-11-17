import { createApp } from 'vue';
import App from './App.vue';
import { setupDesktop } from '@desktop/env.js';
import { initTheme } from '@/utils/themeUtil';
import { initDialogDrag } from '@/utils/dialogDrag';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import '@/styles/main.scss';
import '@/styles/theme.scss';
import '@/styles/element.scss';
import '@/styles/shell.scss';
import '@desktop/desktop.scss';
import * as ElIcon from '@element-plus/icons-vue';
import router from '@/router/index';
import store from '@/store/index';
import locale from 'element-plus/es/locale/lang/zh-cn';
// 自定义指令
import { installDebounceDirectives } from '@/directive/debounce.js';
import { installPermissionDirectives } from '@/directive/hasPermi.js';
// 表单设计器组件
import { installCustomFormComponents } from '@/views/formDesigner/installCustomFormComponents.js';
// 引用v-md-editor
import VMdEditor from '@kangc/v-md-editor';
import VMdPreview from '@kangc/v-md-editor/lib/preview.js';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import '@kangc/v-md-editor/lib/theme/style/github.css';
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
import hljs from '@/utils/highlight';
VMdEditor.use(githubTheme, { Hljs: hljs });
VMdPreview.use(githubTheme, { Hljs: hljs });

const app = createApp(App);
// 桌面端适配初始化
setupDesktop();
// 初始化主题：独立路由（设计器、预览等）不经过导航栏，需在挂载前应用并跟随窗口间切换
initTheme();
// 全局弹窗按住标题栏可拖动
initDialogDrag();
app.use(VMdEditor);
app.use(VMdPreview);
app.use(ElementPlus, {locale})
app.use(router) //注册路由
app.use(store) // 注册 Vuex store
// 统一注册icon图标
Object.keys(ElIcon).forEach((key) => {
  app.component(key, ElIcon[key])
})
// 注册表单设计器组件
installCustomFormComponents(app);
// 注册防抖指令
installDebounceDirectives(app);
// 注册权限指令
installPermissionDirectives(app);

app.mount('#app')
