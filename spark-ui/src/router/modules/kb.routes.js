// router/modules/kb.routes.js
import kb from '@/pages/kb/index.vue'
import search from '@/pages/kb/search/index.vue'
import knowledge from '@/pages/kb/knowledge/index.vue'
import knowledgeDocument from '@/pages/kb/document/index.vue'

export default [
    {
        path: '/kb',
        name: 'kb',
        redirect: '/kb/search',
        component: kb,
        meta: {
            title: '知识库'
        },
        children: [
            {
                path: '/kb/search',
                name: 'search',
                component: search,
                meta: {
                    title: '知识文档'
                }
            },
            {
                path: '/kb/knowledge',
                name: 'knowledge',
                component: knowledge,
                meta: {
                    title: '知识库'
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
        ]
    }
]
