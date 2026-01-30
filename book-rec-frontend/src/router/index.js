import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

/**
 * 【解决报错】重写路由的 push 和 replace 方法
 * 解决在当前路由下再次点击跳转同一路由导致的 NavigationDuplicated 报错
 */
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') throw err
  })
}

const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') throw err
  })
}

const routes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/home', // 默认跳到首页
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'book/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
      { path: 'rank', name: 'Rank', component: () => import('../views/Rank.vue') },
      { path: 'square', name: 'Square', component: () => import('../views/Square.vue') },
      { path: 'category', name: 'Category', component: () => import('../views/Category.vue') },
    ]
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      { 
        path: 'dashboard', 
        name: 'Dashboard', 
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '数据大屏' }
      },
      { 
        path: 'user', 
        name: 'UserManage', 
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      { 
        path: 'book', 
        name: 'BookManage', 
        component: () => import('../views/admin/BookManage.vue'),
        meta: { title: '图书管理' }
      },
      { 
        path: 'comment', 
        name: 'CommentManage',
        component: () => import('../views/admin/CommentManage.vue'),
        meta: { title: '评论管理' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register', // 添加注册路由
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
   {
    path: '/interest',
    name: 'Interest',
    component: () => import('../views/Interest.vue')
  },
  {
    // 这里的 :bookId 就是占位符，告诉 Vue 这是一个参数
    path: '/read/:bookId', 
    name: 'Read',
    component: () => import('../views/Read.vue')
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router