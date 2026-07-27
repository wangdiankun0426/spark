import manage from '@/pages/manage/index'
import user from '@/pages/manage/system/user'
import dept from '@/pages/manage/system/dept'
import role from '@/pages/manage/system/role'
import notice from '@/pages/manage/system/notice'
import logLogin from '@/pages/manage/log/login'
import logOperate from '@/pages/manage/log/operate'
import chatMsgManage from '@/pages/manage/chat/msgManage'
import formTemplate from '@/pages/manage/form/template'
import flowTemplate from '@/pages/manage/flow/template'
import provider from '@/pages/manage/llm/provider.vue'
import model from '@/pages/manage/llm/model.vue'
import agent from '@/pages/manage/llm/agent.vue'
import mcp from '@/pages/manage/llm/mcp.vue'
import kbKnowledge from '@/pages/manage/kb/knowledge.vue'
import kbDocument from '@/pages/manage/kb/document.vue'
import kgGraph from '@/pages/manage/kg/graph.vue'
import kgEntity from '@/pages/manage/kg/entity.vue'
import kgRelation from '@/pages/manage/kg/relation.vue'

export default [
    {
        path: '/manage',
        name: 'manage',
        redirect: '/manage/system/user',
        component: manage,
        meta: { title: '管理后台' },
        children: [
            {
                path: '/manage/system/user',
                name: 'user',
                component: user,
                meta: { title: '用户管理' }
            },
            {
                path: '/manage/system/dept',
                name: 'dept',
                component: dept,
                meta: { title: '部门管理' }
            },
            {
                path: '/manage/system/role',
                name: 'role',
                component: role,
                meta: { title: '角色管理' }
            },
            {
                path: '/manage/system/notice',
                name: 'notice',
                component: notice,
                meta: { title: '公告管理' }
            },
            {
                path: '/manage/llm/providerManage',
                name: 'providerManage',
                component: provider,
                meta: { title: '厂商管理' }
            },
            {
                path: '/manage/llm/modelManage',
                name: 'modelManage',
                component: model,
                meta: { title: '模型管理' }
            },
            {
                path: '/manage/llm/mcpManage',
                name: 'mcpManage',
                component: mcp,
                meta: { title: 'MCP管理' }
            },
            {
                path: '/manage/llm/agentManage',
                name: 'agentManage',
                component: agent,
                meta: { title: 'Agent管理' }
            },
            {
                path: '/manage/log/login',
                name: 'logLogin',
                component: logLogin,
                meta: { title: '登录日志' }
            },
            {
                path: '/manage/log/operate',
                name: 'logOperate',
                component: logOperate,
                meta: { title: '操作日志' }
            },
            {
                path: '/manage/chat/msgManage',
                name: 'chatMsgManage',
                component: chatMsgManage,
                meta: { title: '聊天记录' }
            },
            {
                path: '/manage/form/template',
                name: 'formTemplate',
                component: formTemplate,
                meta: { title: '表单模板' }
            },
            {
                path: '/manage/flow/template',
                name: 'flowTemplate',
                component: flowTemplate,
                meta: { title: '流程模板' }
            },
            {
                path: '/manage/kb/knowledge',
                name: 'manageKbKnowledge',
                component: kbKnowledge,
                meta: { title: '知识库' }
            },
            {
                path: '/manage/kb/document',
                name: 'manageKbDocument',
                component: kbDocument,
                meta: { title: '知识文档' }
            },
            {
                path: '/manage/kg/graph',
                name: 'manageKgGraph',
                component: kgGraph,
                meta: { title: '知识图谱' }
            },
            {
                path: '/manage/kg/entity',
                name: 'manageKgEntity',
                component: kgEntity,
                meta: { title: '实体管理' }
            },
            {
                path: '/manage/kg/relation',
                name: 'manageKgRelation',
                component: kgRelation,
                meta: { title: '关系管理' }
            },
        ]
    }
]