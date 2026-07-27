import flow from '@/pages/flow/index.vue'
import flowApplication from '@/pages/flow/flowApplication/index.vue'
import myAppliedList from '@/pages/flow/myAppliedList/index.vue'
import myPendingList from '@/pages/flow/myPendingList/index.vue'
import myPendedList from '@/pages/flow/myPendedList/index.vue'

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
                path: '/flow/myAppliedList',
                name: 'myAppliedList',
                component: myAppliedList
            },
            {
                path: '/flow/myPendingList',
                name: 'myPendingList',
                component: myPendingList
            },
            {
                path: '/flow/myPendedList',
                name: 'myPendedList',
                component: myPendedList
            }
        ]
    }
]