// router/modules/layout.routes.js
import index from '@/pages/index.vue'
import llmRoutes from "@/router/modules/llm.routes.js";
import kbRoutes from "@/router/modules/kb.routes.js";
import kgRoutes from "@/router/modules/kg.routes.js";
import flowRoutes from "@/router/modules/flow.routes.js";
import manageRoutes from "@/router/modules/manage.routes.js";
import siteManages from "@/router/modules/siteManage.routes.js";

export default [
    {
        path: '/',
        name: 'index',
        component: index,
        redirect: '/home',
        children: [
            {
                path: '/home',
                name: 'home',
                component: () => import('@/pages/home/index'),
                meta: {
                    title: '工作台'
                },
                children: [
                    {
                        path: '/home/index',
                        name: 'home',
                        component: () => import('@/pages/home/index')
                    }
                ]
            },
            ...llmRoutes,
            ...kbRoutes,
            ...kgRoutes,
            ...flowRoutes,
            ...manageRoutes,
            ...siteManages,
        ]
    }
]