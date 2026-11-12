import home from '@/pages/home/index.vue'
import {isDesktop} from '@desktop/env.js'
import dashboard from '@/pages/home/dashboard/index.vue'
import flowApplication from '@/pages/home/flow/application/index.vue'
import flowMyApplication from '@/pages/home/flow/myApplication/index.vue'
import flowMyTodo from '@/pages/home/flow/myTodo/index.vue'
import flowMyDone from '@/pages/home/flow/myDone/index.vue'
import flowCopyMe from '@/pages/home/flow/copyMe/index.vue'
import contact from '@/pages/home/contact/index.vue'
import aichat from '@/pages/home/aichat/index.vue'
import homeAgent from '@/pages/home/agent/index.vue'
import homeWorkflow from '@/pages/home/workflow/index.vue'
import homeWorkflowInstance from '@/pages/home/workflow/instance.vue'
import homeWorkflowReview from '@/pages/home/workflow/review.vue'

export default [
    {
        path: '/home',
        name: 'home',
        redirect: '/home/dashboard',
        // 客户端侧栏由独立外壳承担，这里只保留子路由出口，避免出现双份导航；
        // 浏览器端仍使用带 82px 图标栏的原组件
        component: isDesktop ? () => import('@desktop/pages/homeContainer.vue') : home,
        meta: {
            title: '工作台'
        },
        children: [
            {
                path: '/home/dashboard',
                name: 'homeDashboard',
                component: dashboard,
                meta: {
                    title: '工作台',
                }
            },
            {
                path: '/home/contact',
                name: 'contact',
                component: contact,
                meta: {
                    title: '通讯录',
                }
            },
            {
                path: '/home/aichat',
                name: 'aichat',
                component: aichat,
                meta: {
                    title: 'AI对话',
                }
            },
            {
                path: '/home/agent',
                name: 'homeAgent',
                component: homeAgent,
                meta: {
                    title: '智能体',
                }
            },
            {
                path: '/home/workflow',
                name: 'homeWorkflow',
                component: homeWorkflow,
                meta: {
                    title: '工作流',
                }
            },
            {
                path: '/home/workflow/instance',
                name: 'homeWorkflowInstance',
                component: homeWorkflowInstance,
                meta: {
                    title: '运行实例',
                }
            },
            {
                path: '/home/workflow/review',
                name: 'homeWorkflowReview',
                component: homeWorkflowReview,
                meta: {
                    title: '待审核工作流',
                }
            },

            {
                path: '/home/flow/application',
                name: 'homeFlowApplication',
                component: flowApplication,
                meta: {
                    title: '流程申请'
                }
            },
            {
                path: '/home/flow/myApplication',
                name: 'homeFlowMyApplication',
                component: flowMyApplication,
                meta: {
                    title: '我的申请'
                }
            },
            {
                path: '/home/flow/myTodo',
                name: 'homeFlowMyTodo',
                component: flowMyTodo,
                meta: {
                    title: '我的待办'
                }
            },
            {
                path: '/home/flow/myDone',
                name: 'homeFlowMyDone',
                component: flowMyDone,
                meta: {
                    title: '我的已办'
                }
            },
            {
                path: '/home/flow/copyMe',
                name: 'homeFlowCopyMe',
                component: flowCopyMe,
                meta: {
                    title: '抄送给我'
                }
            },
        ]
    }
]
