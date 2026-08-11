import { createApp } from 'vue';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import '@/styles/main.scss';
import '@/styles/element.scss';
import * as ElIcon from '@element-plus/icons-vue';
import router from '@/router/index';
import store from '@/store/index';
import locale from 'element-plus/es/locale/lang/zh-cn';
// 自定义组件
import CustomInput from '@/views/form/designer/formWidget/basicWidget/input.vue';
import CustomTextarea from '@/views/form/designer/formWidget/basicWidget/textarea.vue';
import CustomRadio from '@/views/form/designer/formWidget/basicWidget/radio.vue';
import CustomSelect from '@/views/form/designer/formWidget/basicWidget/select.vue';
import CustomSelectUser from '@/views/form/designer/formWidget/basicWidget/selectUser.vue';
import CustomDeptUser from '@/views/form/designer/formWidget/basicWidget/selectDept.vue';
import CustomSelectRole from '@/views/form/designer/formWidget/basicWidget/selectRole.vue';
// 自定义组件配置组件
import CustomInputSetting from '@/views/form/designer/settingPanel/basicWidget/input.vue';
import CustomTextareaSetting from '@/views/form/designer/settingPanel/basicWidget/textarea.vue';
import CustomRadioSetting from '@/views/form/designer/settingPanel/basicWidget/radio.vue';
import CustomSelectSetting from '@/views/form/designer/settingPanel/basicWidget/select.vue';
import CustomSelectUserSetting from '@/views/form/designer/settingPanel/basicWidget/selectUser.vue';
import CustomSelectDeptSetting from '@/views/form/designer/settingPanel/basicWidget/selectDept.vue';
import CustomSelectRoleSetting from '@/views/form/designer/settingPanel/basicWidget/selectRole.vue';
const components = [
    // 组件
  CustomInput,
  CustomTextarea,
  CustomRadio,
  CustomSelect,
  CustomSelectUser,
  CustomDeptUser,
  CustomSelectRole,
    // 组件配置
  CustomInputSetting,
  CustomTextareaSetting,
  CustomRadioSetting,
  CustomSelectSetting,
  CustomSelectUserSetting,
  CustomSelectDeptSetting,
  CustomSelectRoleSetting,
];
// 引用v-md-editor
import VMdEditor from '@kangc/v-md-editor';
import VMdPreview from '@kangc/v-md-editor/lib/preview.js';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import '@kangc/v-md-editor/lib/theme/style/github.css';
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
// github theme 不内置 highlight.js，必须显式传入 Hljs 实例，否则渲染代码块时 hasLang 会抛
// "Cannot read properties of undefined (reading 'getLanguage)"，详见 src/utils/highlight.js
import hljs from '@/utils/highlight';

VMdEditor.use(githubTheme, { Hljs: hljs });
VMdPreview.use(githubTheme, { Hljs: hljs });

const app = createApp(App);
app.use(VMdEditor);
app.use(VMdPreview);
app.use(ElementPlus, {locale})
app.use(router) //注册路由
app.use(store) // 注册 Vuex store
// 统一注册icon图标
Object.keys(ElIcon).forEach((key) => {
  app.component(key, ElIcon[key])
})
// 注册自定义组件
components.forEach((Component) => {
  app.component(Component.name, Component);
});

app.mount('#app')
