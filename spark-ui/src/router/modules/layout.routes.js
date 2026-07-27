// router/modules/layout.routes.js
import index from '@/pages/index'

export default [
    {
        path: '/',
        name: 'index',
        component: index,
        redirect: '/home',
        children: []
    }
]