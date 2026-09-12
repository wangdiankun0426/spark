import site from '@/pages/siteManage/index'
import tenant from "@/pages/siteManage/sys/tenant.vue";
import tenantConfig from "@/pages/siteManage/sys/tenantConfig.vue";

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
            {
                path: '/site/sys/tenant/config',
                name: 'tenantConfig',
                component: tenantConfig,
                meta: {
                    title: '租户配置'
                }
            },
        ]
    }
]