// router/modules/kb.routes.js
import kb from '@/pages/kb/index.vue'
import search from '@/pages/kb/search/index.vue'
import knowledge from '@/pages/kb/knowledge/index.vue'
import knowledgeDocument from '@/views/dms/document/index.vue'
import kbTest from '@/pages/kb/test/index.vue'
import kbStats from '@/pages/kb/stats/index.vue'

export default [
    {
        path: '/kb',
        name: 'kb',
        redirect: '/kb/knowledge',
        component: kb,
        meta: {
            title: '知识库'
        },
        children: [
            {
                path: '/kb/knowledge',
                name: 'knowledge',
                component: knowledge,
                meta: {
                    title: '知识库'
                }
            },
            {
                path: '/kb/search',
                name: 'search',
                component: search,
                meta: {
                    title: '知识文档'
                }
            },
            {
                path: '/kb/knowledge/document',
                name: 'knowledgeDocument',
                component: knowledgeDocument,
                meta: {
                    title: '知识库文档'
                }
            },
            {
                path: '/kb/test',
                name: 'kbTest',
                component: kbTest,
                meta: {
                    title: '检索测试'
                }
            },
            {
                path: '/kb/stats',
                name: 'kbStats',
                component: kbStats,
                meta: {
                    title: '检索统计'
                }
            },
        ]
    }
]
