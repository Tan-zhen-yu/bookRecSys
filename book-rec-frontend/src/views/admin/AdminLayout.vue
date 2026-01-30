<template>
  <div class="admin-layout-wrapper">
    <!-- 左侧侧边栏 -->
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <div class="logo-icon">
          <i class="el-icon-monitor"></i>
        </div>
        <span class="logo-text">管理控制台</span>
      </div>
      
      <div class="menu-container">
        <el-menu
          background-color="transparent"
          text-color="rgba(255,255,255,0.6)"
          active-text-color="#fff"
          router
          unique-opened
          :default-active="$route.path"
          class="custom-admin-menu">
          
          <el-menu-item index="/admin/dashboard">
            <i class="el-icon-pie-chart"></i>
            <span slot="title">数据大屏</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/user">
            <i class="el-icon-user-solid"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/book">
            <i class="el-icon-collection"></i>
            <span slot="title">图书管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/comment">
            <i class="el-icon-chat-dot-square"></i>
            <span slot="title">评论管理</span>
          </el-menu-item>

          <div class="menu-divider"></div>

          <el-menu-item index="/home" class="return-home">
            <i class="el-icon-back"></i>
            <span slot="title">返回前台门户</span>
          </el-menu-item>
        </el-menu>
      </div>
    </aside>

    <!-- 右侧主体 -->
    <main class="admin-main-container">
      <!-- 顶部 Header -->
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>后台管理</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-profile">
              <el-avatar size="small" icon="el-icon-user-solid" class="mgr-10"></el-avatar>
              <span class="user-name">系统管理员</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
            <el-dropdown-menu slot="dropdown" class="admin-dropdown">
              <el-dropdown-item icon="el-icon-house" @click.native="$router.push('/home')">返回首页</el-dropdown-item>
              <el-dropdown-item icon="el-icon-switch-button" divided @click.native="handleLogout">安全退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <section class="admin-content-view">
        <div class="content-scroll-area">
          <transition name="fade-transform" mode="out-in">
            <router-view />
          </transition>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
export default {
  name: "AdminLayout",
  computed: {
    currentRouteName() {
      const map = {
        '/admin/dashboard': '数据大屏',
        '/admin/user': '用户管理',
        '/admin/book': '图书管理',
        '/admin/comment': '评论管理'
      };
      return map[this.$route.path] || '系统预览';
    }
  },
  methods: {
    handleLogout() {
      localStorage.removeItem("user");
      this.$router.push("/login");
      this.$message.success("已退出系统");
    }
  }
}
</script>

<style scoped>
/* ================= 全局变量与容器 ================= */
.admin-layout-wrapper {
  --sidebar-width: 240px;
  --header-height: 64px;
  --primary-color: #409EFF;
  --bg-dark: #1a1d21; /* 极客黑 */
  --bg-light: #f6f8fb;
  
  display: flex;
  height: 100vh;
  background-color: var(--bg-light);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* ================= 左侧侧边栏 ================= */
.admin-sidebar {
  width: var(--sidebar-width);
  background-color: var(--bg-dark);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 10px rgba(0,0,0,0.1);
  z-index: 100;
  transition: all 0.3s;
}

.admin-logo {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
}

.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}

.menu-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
}

/* 隐藏滚动条 */
.menu-container::-webkit-scrollbar { width: 0; }

.custom-admin-menu {
  border: none !important;
}

/* 菜单项高级样式 */
.custom-admin-menu >>> .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.custom-admin-menu >>> .el-menu-item i {
  color: inherit;
  margin-right: 12px;
  font-size: 18px;
}

.custom-admin-menu >>> .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.05) !important;
  color: #fff !important;
}

.custom-admin-menu >>> .el-menu-item.is-active {
  background: var(--primary-color) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.menu-divider {
  height: 1px;
  background: rgba(255,255,255,0.05);
  margin: 15px 12px;
}

.return-home {
  opacity: 0.8;
}

/* ================= 右侧主体内容 ================= */
.admin-main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* Header 样式 */
.admin-header {
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px); /* 毛玻璃 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  z-index: 99;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 12px;
  border-radius: 30px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: #f0f2f5;
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 内容显示区 */
.admin-content-view {
  flex: 1;
  padding: 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.content-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px; /* 预留滚动条空间 */
}

/* 美化滚动条 */
.content-scroll-area::-webkit-scrollbar {
  width: 6px;
}
.content-scroll-area::-webkit-scrollbar-thumb {
  background: #e1e4e8;
  border-radius: 3px;
}

/* ================= 辅助样式 ================= */
.mgr-10 { margin-right: 10px; }

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* Dropdown 样式 */
.admin-dropdown {
  border-radius: 12px !important;
  padding: 8px 0 !important;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
  border: none !important;
}
</style>