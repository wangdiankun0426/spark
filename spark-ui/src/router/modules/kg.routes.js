// router/modules/kg.routes.js
import kg from '@/pages/kg/index.vue'
import graph from '@/pages/kg/graph/index.vue'
import graphDocument from '@/pages/kg/document/index.vue'
import graphSearch from '@/pages/kg/search/index.vue'
import kgEntity from '@/pages/kg/entity/index.vue'
import kgRelation from '@/pages/kg/relation/index.vue'
import kgAnalysis from '@/pages/kg/analysis/index.vue'
import kgAudit from '@/pages/kg/audit/index.vue'

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
                    title: '知识图谱'
                }
            },
            {
                path: '/kg/search',
                name: 'graphSearch',
                component: graphSearch,
                meta: {
                    title: '文档检索'
                }
            },
            {
                path: '/kg/graph/document',
                name: 'graphDocument',
                component: graphDocument,
                meta: {
                    title: '图谱文档'
                }
            },
            {
                path: '/kg/entity',
                name: 'kgEntity',
                component: kgEntity,
                meta: {
                    title: '实体'
                }
            },
            {
                path: '/kg/relation',
                name: 'kgRelation',
                component: kgRelation,
                meta: {
                    title: '关系'
                }
            },
            {
                path: '/kg/audit',
                name: 'kgAudit',
                component: kgAudit,
                meta: {
                    title: '实体审核'
                }
            },
            {
                path: '/kg/analysis',
                name: 'kgAnalysis',
                component: kgAnalysis,
                meta: {
                    title: '图谱分析'
                }
            },
        ]
    }
]
