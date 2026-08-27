// router/modules/llm.routes.js
import llm from '@/pages/llm/index.vue'
import agent from '@/pages/llm/agent/index.vue'
import modelMarket from '@/pages/llm/modelMarket/index.vue'
import workflowHub from '@/pages/llm/workflow/index.vue'
import WorkflowInstance from '@/pages/llm/workflow/instance.vue'

export default [
    {
        path: '/llm',
        name: 'llm',
        redirect: '/llm/agent',
        component: llm,
        meta: {
            title: 'AI应用'
        },
        children: [
            {
                path: '/llm/agent',
                name: 'agent',
                component: agent,
                meta: {
                    title: 'Agent'
                }
            },
            {
                path: '/llm/workflow',
                name: 'llmWorkflow',
                component: workflowHub,
                meta: {
                    title: 'WorkFlow'
                }
            },
            {
                path: '/llm/modelMarket',
                name: 'modelMarket',
                component: modelMarket,
                meta: {
                    title: '模型市场'
                }
            },
            {
                path: '/llm/workflow/instance',
                name: 'workflowInstance',
                component: WorkflowInstance,
                meta: {
                    title: '我的运行'
                }
            },
        ]
    }
]