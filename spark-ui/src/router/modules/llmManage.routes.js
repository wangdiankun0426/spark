
import llm from '@/pages/llmMange/index.vue'
import agent from '@/pages/llmMange/agent/index.vue'
import modelMarket from '@/pages/llmMange/modelMarket/index.vue'
import skill from '@/pages/llmMange/skill/index.vue'
import mcp from '@/pages/llmMange/mcp/index.vue'
import workflowHub from '@/pages/llmMange/workflow/index.vue'
import WorkflowInstance from '@/pages/llmMange/workflow/instance.vue'
import knowledge from "@/pages/llmMange/knowledge/index.vue";
import graph from "@/pages/llmMange/graph/index.vue";
import knowledgeDocument from "@/views/dms/document/index.vue";
import graphDetail from "@/pages/llmMange/graph/graphDetail/index.vue";
import graphDocument from "@/views/dms/document/index.vue";
import kgEntity from "@/pages/llmMange/graph/entity/index.vue";
import kgRelation from "@/pages/llmMange/graph/relation/index.vue";
import {MENU_CODES} from "@/utils/menuUtil.js";

export default [
    {
        path: '/llm',
        name: 'llm',
        redirect: '/llm/agent',
        component: llm,
        meta: {
            title: 'AI管理'
        },
        children: [
            {
                path: '/llm/agent',
                name: 'agent',
                component: agent,
                meta: {
                    title: 'Agent',
                    menuCode: MENU_CODES.LLM_AGENT
                }
            },
            {
                path: '/llm/workflow',
                name: 'llmWorkflow',
                component: workflowHub,
                meta: {
                    title: 'WorkFlow',
                    menuCode: MENU_CODES.LLM_WORKFLOW
                }
            },
            {
                path: '/llm/modelMarket',
                name: 'modelMarket',
                component: modelMarket,
                meta: {
                    title: '模型市场',
                    menuCode: MENU_CODES.LLM_MODEL_MARKET
                }
            },
            {
                path: '/llm/skill',
                name: 'llmSkill',
                component: skill,
                meta: {
                    title: '技能库',
                    menuCode: MENU_CODES.LLM_SKILL
                }
            },
            {
                path: '/llm/mcp',
                name: 'llmMcp',
                component: mcp,
                meta: {
                    title: 'MCP服务',
                    menuCode: MENU_CODES.LLM_MCP
                }
            },
            {
                path: '/llm/workflow/instance',
                name: 'workflowInstance',
                component: WorkflowInstance,
                meta: {
                    title: '运行记录',
                }
            },
            {
                path: '/llm/knowledge',
                name: 'knowledge',
                component: knowledge,
                meta: {
                    title: '知识库',
                    menuCode: MENU_CODES.LLM_KNOWLEDGE
                }
            },
            {
                path: '/llm/knowledge/document',
                name: 'knowledgeDocument',
                component: knowledgeDocument,
                meta: {
                    title: '知识库文档',
                }
            },
            {
                path: '/llm/graph',
                name: 'graph',
                component: graph,
                meta: {
                    title: '知识图谱',
                    menuCode: MENU_CODES.LLM_GRAPH
                }
            },
            {
                path: '/llm/graph/detail',
                name: 'graphDetail',
                component: graphDetail,
                meta: {
                    title: '图谱详情',
                }
            },
            {
                path: '/llm/graph/document',
                name: 'graphDocument',
                component: graphDocument,
                meta: {
                    title: '图谱文档',
                }
            },
            {
                path: '/llm/graph/entity',
                name: 'kgEntity',
                component: kgEntity,
                meta: {
                    title: '图谱实体',
                }
            },
            {
                path: '/llm/graph/relation',
                name: 'kgRelation',
                component: kgRelation,
                meta: {
                    title: '图谱关系',
                }
            },
        ]
    }
]