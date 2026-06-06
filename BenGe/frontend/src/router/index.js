import { createRouter, createWebHistory } from 'vue-router';

const routes = [
    {
        path: '/',
        name: 'loginPage',
        component: () => import('@/view/loginPage.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/home',
        name: 'homePage',
        component: () => import('@/view/MainPage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/single',
        name: 'singlePro',
        component: () => import('@/view/SinglePro.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/cooperate',
        name: 'cooperatePro',
        component: () => import('@/view/CooperatePro.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/room/:roomId?',
        name: 'roomPage',
        component: () => import('@/view/RoomPage.vue'),
        meta: { requiresAuth: true }
    },
    // 测试用
    {
        path: '/canvas',
        name: 'canvas',
        component: () => import('@/components/Cooperation/second/roles/NarrativeWorkspace.vue'),
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
    const isAuthenticated = localStorage.getItem('token');
    
    // 需要认证的路由
    if (to.meta.requiresAuth && !isAuthenticated) {
        // 如果需要认证但没有token，重定向到登录页
        next({ name: 'loginPage' });
    } else if (to.name === 'loginPage' && isAuthenticated) {
        // 如果已登录，访问登录页会重定向到首页
        next({ name: 'homePage' });
    } else {
        // 正常导航
        next();
    }
});

export default router;