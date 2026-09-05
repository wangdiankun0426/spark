import manage from '@/pages/manage/index'
import user from '@/pages/manage/sys/user'
import dept from '@/pages/manage/sys/dept'
import role from '@/pages/manage/sys/role'
import notice from '@/pages/manage/sys/notice'
import message from '@/pages/manage/sys/message'
import logLogin from '@/pages/manage/log/login'
import logOperate from '@/pages/manage/log/operate'

export default [
    {
        path: '/manage',
        name: 'manage',
        redirect: '/manage/sys/user',
        component: manage,
        meta: {
            title: '管理后台'
        },
        children: [
            {
                path: '/manage/sys/user',
                name: 'user',
                component: user,
                meta: {
                    title: '用户管理'
                }
            },
            {
                path: '/manage/sys/dept',
                name: 'dept',
                component: dept,
                meta: {
                    title: '部门管理'
                }
            },
            {
                path: '/manage/sys/role',
                name: 'role',
                component: role,
                meta: {
                    title: '角色管理'
                }
            },
            {
                path: '/manage/sys/notice',
                name: 'notice',
                component: notice,
                meta: {
                    title: '公告管理'
                }
            },
            {
                path: '/manage/sys/message',
                name: 'message',
                component: message,
                meta: {
                    title: '消息管理'
                }
            },
            {
                path: '/manage/log/login',
                name: 'logLogin',
                component: logLogin,
                meta: {
                    title: '登录日志'
                }
            },
            {
                path: '/manage/log/operate',
                name: 'logOperate',
                component: logOperate,
                meta: {
                    title: '操作日志'
                }
            },
        ]
    }
]