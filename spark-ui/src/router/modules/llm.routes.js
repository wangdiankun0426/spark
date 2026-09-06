// router/modules/llm.routes.js
import llm from '@/pages/llm/index.vue'
import agent from '@/pages/llm/agent/index.vue'
import modelMarket from '@/pages/llm/modelMarket/index.vue'
import skill from '@/pages/llm/skill/index.vue'
import mcp from '@/pages/llm/mcp/index.vue'
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
                    title: 'Agent',
                    menuId: 201
                }
            },
            {
                path: '/llm/workflow',
                name: 'llmWorkflow',
                component: workflowHub,
                meta: {
                    title: 'WorkFlow',
                    menuId: 202
                }
            },
            {
                path: '/llm/modelMarket',
                name: 'modelMarket',
                component: modelMarket,
                meta: {
                    title: '模型市场',
                    menuId: 203
                }
            },
            {
                path: '/llm/skill',
                name: 'llmSkill',
                component: skill,
                meta: {
                    title: '技能库',
                    menuId: 204
                }
            },
            {
                path: '/llm/mcp',
                name: 'llmMcp',
                component: mcp,
                meta: {
                    title: 'MCP服务',
                    menuId: 205
                }
            },
            {
                path: '/llm/workflow/instance',
                name: 'workflowInstance',
                component: WorkflowInstance,
                meta: {
                    title: '运行记录',
                    menuId: 2021
                }
            },
        ]
    }
]