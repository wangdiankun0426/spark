// router/modules/llm.routes.js
import llm from '@/pages/llm/index.vue'
import agent from '@/pages/llm/agent/index.vue'
import modelMarket from '@/pages/llm/modelMarket/index.vue'

export default [
    {
        path: '/llm',
        name: 'llm',
        redirect: '/llm/agent',
        component: llm,
        meta: {
            title: 'AI 应用'
        },
        children: [
            {
                path: '/llm/agent',
                name: 'agent',
                component: agent,
                meta: { title: 'Agent' }
            },
            {
                path: '/llm/modelMarket',
                name: 'modelMarket',
                component: modelMarket,
                meta: { title: '模型市场' }
            },
        ]
    }
]