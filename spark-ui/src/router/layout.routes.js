// router/modules/layout.routes.js
import index from '@/pages/index.vue'
import llmManageRoutes from "@/router/modules/llmManage.routes.js";
import sysManageRoutes from "@/router/modules/orgManage.routes.js";
import siteManageRoutes from "@/router/modules/siteManage.routes.js";
import busManageRoutes from "@/router/modules/busManage.routes.js";
import homeRoutes from "@/router/modules/home.routes.js";

export default [
    {
        path: '/',
        name: 'index',
        component: index,
        children: [
            ...homeRoutes,
            ...llmManageRoutes,
            ...sysManageRoutes,
            ...siteManageRoutes,
            ...busManageRoutes,
        ]
    }
]