import site from '@/pages/siteManage/index'
import tenant from "@/pages/siteManage/sys/tenant.vue";

export default [
    {
        path: '/site',
        name: 'site',
        redirect: '/site/sys/tenant',
        component: site,
        meta: {
            title: '站点管理'
        },
        children: [
            {
                path: '/site/sys/tenant',
                name: 'tenant',
                component: tenant,
                meta: {
                    title: '租户管理'
                }
            },
        ]
    }
]