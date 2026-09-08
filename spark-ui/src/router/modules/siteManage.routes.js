import siteManage from '@/pages/siteManage/index'
import tenant from "@/pages/siteManage/sys/tenant.vue";

export default [
    {
        path: '/siteManage',
        name: 'siteManage',
        redirect: '/siteManage/sys/tenant',
        component: siteManage,
        meta: {
            title: '站点管理'
        },
        children: [
            {
                path: '/siteManage/sys/tenant',
                name: 'tenant',
                component: tenant,
                meta: {
                    title: '租户管理'
                }
            },
        ]
    }
]