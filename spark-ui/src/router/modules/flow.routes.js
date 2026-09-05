import flow from '@/pages/flow/index.vue'
import flowApplication from '@/pages/flow/flowApplication/index.vue'
import myApplication from '@/pages/flow/myApplication/index.vue'
import myTodo from '@/pages/flow/myTodo/index.vue'
import myDone from '@/pages/flow/myDone/index.vue'
import copyMe from '@/pages/flow/copyMe/index.vue'
import flowInstance from "@/views/flow/instance/index.vue";

export default [
    {
        path: '/flow',
        name: 'flow',
        redirect: '/flow/application',
        component: flow,
        meta: {
            title: '流程中心'
        },
        children: [
            {
                path: '/flow/application',
                name: 'flowApplication',
                component: flowApplication,
                meta: {
                    title: '流程申请'
                },
            },
            {
                path: '/flow/instance',
                name: 'flowInstance',
                component: flowInstance,
                meta: {
                    title: '流程实例'
                }
            },
            {
                path: '/flow/myApplication',
                name: 'myApplication',
                component: myApplication,
                meta: {
                    title: '我的申请'
                },
            },
            {
                path: '/flow/myTodo',
                name: 'myTodo',
                component: myTodo,
                meta: {
                    title: '我的待办'
                },
            },
            {
                path: '/flow/myDone',
                name: 'myDone',
                component: myDone,
                meta: {
                    title: '我的已办'
                },
            },
            {
                path: '/flow/copyMe',
                name: 'copyMe',
                component: copyMe,
                meta: {
                    title: '抄送给我'
                },
            }
        ]
    }
]