import flow from '@/pages/flow/index.vue'
import flowApplication from '@/pages/flow/flowApplication/index.vue'
import myApplication from '@/pages/flow/myApplication/index.vue'
import myTodo from '@/pages/flow/myTodo/index.vue'
import myDone from '@/pages/flow/myDone/index.vue'
import copyMe from '@/pages/flow/copyMe/index.vue'

export default [
    {
        path: '/flow',
        name: 'flow',
        redirect: '/flow/application',
        component: flow,
        meta: { title: '流程中心' },
        children: [
            {
                path: '/flow/application',
                name: 'flowApplication',
                component: flowApplication
            },
            {
                path: '/flow/myApplication',
                name: 'myApplication',
                component: myApplication
            },
            {
                path: '/flow/myTodo',
                name: 'myTodo',
                component: myTodo
            },
            {
                path: '/flow/myDone',
                name: 'myDone',
                component: myDone
            },
            {
                path: '/flow/copyMe',
                name: 'copyMe',
                component: copyMe
            }
        ]
    }
]