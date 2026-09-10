import bus from '@/pages/busManage/index.vue'
import flowTemplate from '@/pages/busManage/flowTemplate/index.vue'
import flowInstance from '@/pages/busManage/flowTemplate/instance.vue'
import {MENU_CODES} from "@/utils/menuUtil.js";

export default [
    {
        path: '/bus',
        name: 'bus',
        redirect: '/bus/flowTemplate',
        component: bus,
        meta: {
            title: '业务管理'
        },
        children: [
            {
                path: '/bus/flowTemplate',
                name: 'flowTemplate',
                component: flowTemplate,
                meta: {
                    title: '流程模板',
                    menuCode: MENU_CODES.BUS_FLOW
                }
            },
            {
                path: '/bus/flowInstance',
                name: 'flowInstance',
                component: flowInstance,
                meta: {
                    title: '流程实例',
                }
            },
        ]
    }
]