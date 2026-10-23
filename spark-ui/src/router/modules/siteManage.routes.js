import site from '@/pages/siteManage/index'
import tenant from "@/pages/siteManage/sys/tenant.vue";
import tenantConfig from "@/pages/siteManage/sys/tenantConfig.vue";
import monitorServer from '@/pages/siteManage/monitor/server.vue'
import monitorCache from '@/pages/siteManage/monitor/cache.vue'
import monitorMq from '@/pages/siteManage/monitor/mq.vue'
import monitorApi from '@/pages/siteManage/monitor/api.vue'

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
            {
                path: '/site/monitor/server',
                name: 'monitorServer',
                component: monitorServer,
                meta: {
                    title: '服务器监控'
                }
            },
            {
                path: '/site/monitor/cache',
                name: 'monitorCache',
                component: monitorCache,
                meta: {
                    title: '缓存监控'
                }
            },
            {
                path: '/site/monitor/mq',
                name: 'monitorMq',
                component: monitorMq,
                meta: {
                    title: '消息队列'
                }
            },
            {
                path: '/site/monitor/api',
                name: 'monitorApi',
                component: monitorApi,
                meta: {
                    title: '接口请求'
                }
            },
        ]
    }
]
