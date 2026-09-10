import home from '@/pages/home/index.vue'
import flowApplication from '@/pages/home/flow/application/index.vue'
import flowMyApplication from '@/pages/home/flow/myApplication/index.vue'
import flowMyTodo from '@/pages/home/flow/myTodo/index.vue'
import flowMyDone from '@/pages/home/flow/myDone/index.vue'
import flowCopyMe from '@/pages/home/flow/copyMe/index.vue'
import contact from '@/pages/home/contact/index.vue'
import aichat from '@/pages/home/aichat/index.vue'
import homeAgent from '@/pages/home/agent/index.vue'
import homeWorkflow from '@/pages/home/workflow/index.vue'

export default [
    {
        path: '/home',
        name: 'home',
        redirect: '/home/aichat',
        component: home,
        meta: {
            title: '工作台'
        },
        children: [
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
                    title: 'AiChat',
                }
            },
            {
                path: '/home/agent',
                name: 'homeAgent',
                component: homeAgent,
                meta: {
                    title: 'Agent',
                }
            },
            {
                path: '/home/workflow',
                name: 'homeWorkflow',
                component: homeWorkflow,
                meta: {
                    title: 'WorkFlow',
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
