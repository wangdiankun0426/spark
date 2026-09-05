// router/modules/common.routes.js
import login from '@/pages/login/login.vue'
import oauthLogin from '@/pages/login/oauthLogin.vue'
import register from '@/pages/login/register.vue'
import formDesigner from '@/views/formDesigner/index.vue'
import noticeEdit from '@/views/notice/edit.vue'
import noticeView from '@/views/notice/view.vue'
import flowDesigner from '@/views/flow/designer/index.vue'
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
        path: '/form/designer/:id/:revId',
        name: 'formDesigner',
        component: formDesigner,
        meta: {
            title: '表单设计器'
        }
    },
    {
        path: '/flow/designer/:id/:revId',
        name: 'flowDesigner',
        component: flowDesigner,
        meta: {
            title: '流程设计器'
        }
    },
    {
        path: '/workflow/designer/:id?/:revId?',
        name: 'workflowDesigner',
        component: workflowIndex,
        meta: {
            title: '工作流设计器'
        }
    },
    {
        path: '/manage/sys/notice/edit/:id',
        name: 'noticeEdit',
        component: noticeEdit,
        meta: {
            title: '编辑公告内容'
        }
    },
    {
        path: '/notice/view/:id',
        name: 'noticeView',
        component: noticeView,
        meta: {
            title: '公告内容'
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
        path: '/noPermission',
        name: 'noPermission',
        component: () => import('@/pages/error/noPermission.vue'),
        meta: {
            title: '无权限'
        }
    },
    {
        path: '/:catchAll(.*)*',
        name: 'noFound',
        component: () => import('@/pages/error/noFound.vue'),
        meta: {
            title: '404'
        }
    }
]