// router/modules/kg.routes.js
import kg from '@/pages/kg/index.vue'
import graph from '@/pages/kg/graph/index.vue'
import graphDocument from '@/views/dms/document/index.vue'
import graphSearch from '@/pages/kg/search/index.vue'
import kgEntity from '@/pages/kg/entity/index.vue'
import kgRelation from '@/pages/kg/relation/index.vue'
import kgAnalysis from '@/pages/kg/analysis/index.vue'
import kgAudit from '@/pages/kg/audit/index.vue'
import graphDetail from "@/pages/kg/graphDetail/index.vue";

export default [
    {
        path: '/kg',
        name: 'kg',
        redirect: '/kg/graph',
        component: kg,
        meta: {
            title: '知识图谱'
        },
        children: [
            {
                path: '/kg/graph',
                name: 'graph',
                component: graph,
                meta: {
                    title: '知识图谱',
                    menuId: 401
                }
            },
            {
                path: '/kg/graph/detail',
                name: 'graphDetail',
                component: graphDetail,
                meta: {
                    title: '图谱详情',
                    menuId: 4011
                }
            },
            {
                path: '/kg/search',
                name: 'graphSearch',
                component: graphSearch,
                meta: {
                    title: '知识检索',
                    menuId: 402
                }
            },
            {
                path: '/kg/graph/document',
                name: 'graphDocument',
                component: graphDocument,
                meta: {
                    title: '图谱文档',
                    menuId: 4012
                }
            },
            {
                path: '/kg/entity',
                name: 'kgEntity',
                component: kgEntity,
                meta: {
                    title: '图谱实体',
                    menuId: 4013
                }
            },
            {
                path: '/kg/relation',
                name: 'kgRelation',
                component: kgRelation,
                meta: {
                    title: '图谱关系',
                    menuId: 4014
                }
            },
            {
                path: '/kg/audit',
                name: 'kgAudit',
                component: kgAudit,
                meta: {
                    title: '实体审核',
                    menuId: 403
                }
            },
            {
                path: '/kg/analysis',
                name: 'kgAnalysis',
                component: kgAnalysis,
                meta: {
                    title: '图谱分析',
                    menuId: 404
                }
            },
        ]
    }
]
