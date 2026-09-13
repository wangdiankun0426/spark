import org from '@/pages/orgMange/index.vue'
import user from '@/pages/orgMange/sys/user'
import dept from '@/pages/orgMange/sys/dept'
import role from '@/pages/orgMange/sys/role'
import message from '@/pages/orgMange/sys/message'
import logLogin from '@/pages/orgMange/log/login'
import logOperate from '@/pages/orgMange/log/operate'
import config from "@/pages/orgMange/sys/config.vue";

export default [
    {
        path: '/org',
        name: 'org',
        redirect: '/org/sys/user',
        component: org,
        meta: {
            title: '组织管理'
        },
        children: [
            {
                path: '/org/sys/user',
                name: 'user',
                component: user,
                meta: {
                    title: '用户管理'
                }
            },
            {
                path: '/org/sys/dept',
                name: 'dept',
                component: dept,
                meta: {
                    title: '部门管理'
                }
            },
            {
                path: '/org/sys/role',
                name: 'role',
                component: role,
                meta: {
                    title: '角色管理'
                }
            },
            {
                path: '/org/sys/message',
                name: 'message',
                component: message,
                meta: {
                    title: '消息管理'
                }
            },
            {
                path: '/org/log/login',
                name: 'logLogin',
                component: logLogin,
                meta: {
                    title: '登录日志'
                }
            },
            {
                path: '/org/log/operate',
                name: 'logOperate',
                component: logOperate,
                meta: {
                    title: '操作日志'
                }
            },
            {
                path: '/org/sys/config',
                name: 'config',
                component: config,
                meta: {
                    title: '系统配置'
                }
            },
        ]
    }
]