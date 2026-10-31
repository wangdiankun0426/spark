import login from '@/pages/login/login.vue'
import oauthLogin from '@/pages/login/oauthLogin.vue'
import register from '@/pages/login/register.vue'
import agreement from '@/pages/base/agreement/index.vue'
import formDesigner from '@/views/formDesigner/index.vue'
import flowDesigner from '@/views/flowDesigner/index.vue'
import workflowIndex from "@/views/workflowDesigner/index.vue";

export default [
    {
        path: '/login',
        component: login,
        meta: {
            title: '登录'
        }
    },
    {
        path: '/oauthLogin',
        component: oauthLogin,
        meta: {
            title: 'OAuth登录'
        }
    },
    {
        path: '/register',
        component: register,
        meta: {
            title: '注册'
        }
    },
    {
        path: '/agreement',
        component: agreement,
        meta: {
            title: '用户协议'
        }
    },
    {
        // 不带版本号：打开表单当前最新版本
        path: '/form/designer/:id',
        name: 'formDesigner',
        component: formDesigner,
        meta: {
            title: '表单设计器'
        }
    },
    {
        // 带版本号：打开表单指定版本
        path: '/form/designer/:id/:revId',
        name: 'formDesignerVersion',
        component: formDesigner,
        meta: {
            title: '表单设计器'
        }
    },
    {
        // 不带版本号：打开流程模板当前最新版本
        path: '/flow/designer/:id',
        name: 'flowDesigner',
        component: flowDesigner,
        meta: {
            title: '流程设计器',
        }
    },
    {
        // 带版本号：打开流程模板指定版本
        path: '/flow/designer/:id/:revId',
        name: 'flowDesignerVersion',
        component: flowDesigner,
        meta: {
            title: '流程设计器',
        }
    },
    {
        path: '/workflow/designer/:id?/:revId?',
        name: 'workflowDesigner',
        component: workflowIndex,
        meta: {
            title: '工作流设计器',
        }
    },
    {
        path: '/document/list',
        name: 'documentList',
        component: () => import('@/views/dms/document/index.vue'),
        meta: {
            title: '文档列表'
        }
    },
    {
        path: '/document/preview',
        name: 'documentPreview',
        component: () => import('@/views/dms/preview/index.vue'),
        meta: {
            title: '文档预览'
        }
    },
    {
        path: '/document/chunk',
        name: 'documentChunk',
        component: () => import('@/views/dms/chunk/index.vue'),
        meta: {
            title: '文档分块'
        }
    },
    {
        path: '/document/editor',
        name: 'documentEditor',
        component: () => import('@/views/dms/editor/index.vue'),
        meta: {
            title: '文档编辑'
        }
    },
    {
        path: '/noPermission',
        name: 'noPermission',
        component: () => import('@/pages/base/error/noPermission.vue'),
        meta: {
            title: '无权限'
        }
    },
    {
        path: '/:catchAll(.*)*',
        name: 'noFound',
        component: () => import('@/pages/base/error/noFound.vue'),
        meta: {
            title: '404'
        }
    }
]