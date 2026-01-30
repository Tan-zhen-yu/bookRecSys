<template>
  <div class="layout-wrapper">
    <!-- 顶部导航栏：固定在顶部，带有毛玻璃效果 -->
    <header class="nav-header">
      <div class="header-inner">
        
        <!-- 1. 左侧 Logo -->
        <div class="logo-area" @click="$router.push('/home')">
          <span class="logo-icon">📚</span>
          <span class="logo-text">云深书屋</span> <!-- 建议给系统起个好听的名字 -->
        </div>

        <!-- 2. 中间导航菜单 -->
        <div class="nav-center">
          <el-menu 
            :default-active="$route.path" 
            mode="horizontal" 
            router 
            class="custom-menu">
            
            <el-menu-item index="/home">首页</el-menu-item>
            <el-menu-item index="/rank">排行榜</el-menu-item>
            <el-menu-item index="/category">分类库</el-menu-item>
            <el-menu-item index="/square">书评广场</el-menu-item>
          </el-menu>
        </div>

        <!-- 3. 右侧用户区域 -->
        <div class="user-area">
          <template v-if="user.id">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-profile-trigger">
                <!-- 头像：如果没有头像链接，显示名字首字 -->
                <el-avatar 
                  :size="36" 
                  :src="user.avatarUrl" 
                  style="background-color: var(--color-primary); color: white;">
                  {{ user.nickname ? user.nickname.charAt(0) : 'User' }}
                </el-avatar>
                <span class="username">{{ user.nickname }}</span>
                <i class="el-icon-arrow-down el-icon--right"></i>
              </div>
              
              <el-dropdown-menu slot="dropdown" class="custom-dropdown">
                <el-dropdown-item command="profile" icon="el-icon-user">个人中心</el-dropdown-item>
              
                <el-dropdown-item divided command="logout" icon="el-icon-switch-button" style="color: #ff6b6b;">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
          
          <template v-else>
            <div class="auth-buttons">
              <el-button type="text" class="login-btn" @click="$router.push('/login')">登录</el-button>
              <el-button type="primary" size="small" round @click="$router.push('/register')">注册</el-button>
            </div>
          </template>
        </div>

      </div>
    </header>

    <!-- 页面主体内容：路由出口 -->
    <main class="main-content">
      <!-- 这里的 transition 是页面级切换动画，配合 App.vue 使用 -->
      <transition name="fade-slide" mode="out-in">
        <router-view />
      </transition>
    </main>

    <!-- 底部页脚 (可选，增加完整度) -->
    <footer class="app-footer">
      <p>© 2024 云深书屋 Book Recommendation System</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: "Layout",
  data() {
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {}
    }
  },
   created() {
    // 监听 'updateUser' 事件
    this.$root.$on('updateUser', (newUser) => {
      this.user = newUser; // 实时更新 Layout 的数据
    });
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.logout();
      } else if (command === 'profile') {
        this.$router.push('/profile');
      } else if (command === 'bookshelf') {
        // 如果你有书架页面
        // this.$router.push('/bookshelf'); 
        this.$message.info("开发中...");
      }
    },
    logout() {
      localStorage.removeItem("user");
      this.$router.push("/login");
      // 建议不要直接 reload，体验不好，Vue 数据驱动即可
      this.user = {}; 
      this.$message.success("已安全退出");
    }
  }
}
</script>

<style scoped>
/* =========================================
   布局容器
   ========================================= */
.layout-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-body); /* 确保底色一致 */
}

/* =========================================
   Header 样式
   ========================================= */
.nav-header {
  position: sticky; /* 粘性定位，随页面滚动吸顶 */
  top: 0;
  z-index: 1000;
  width: 100%;
  height: 64px;
  background: rgba(255, 255, 255, 0.85); /* 增加一点不透明度 */
  box-shadow: 0 1px 0 rgba(0,0,0,0.05); /* 极淡的分割线 */
}

.header-inner {
  max-width: 1200px; /* 与内容区对齐 */
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

/* Logo */
/* 找到这个类，替换为以下内容 */
.logo-area {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: opacity 0.3s;
  
  /* --- 新增修复样式 --- */
  flex-shrink: 0;           /* 防止被挤压 */
  background: transparent !important; /* 强制背景透明，消除白块 */
  user-select: none;        /* 防止点击时出现选中高亮块 */
  border: none;             /* 去除任何可能的边框 */
  outline: none;            /* 去除点击时的轮廓线 */
}
.logo-area:hover {
  opacity: 0.8;
}
.logo-icon {
  font-size: 24px;
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main); /* 使用全局变量 */
  letter-spacing: -0.5px;
    white-space: nowrap; /* 防止换行 */
}

/* 导航菜单魔改 */
.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

/* 穿透修改 Element Menu 样式，去除底部横线和默认背景 */
.custom-menu.el-menu {
  border-bottom: none !important;
  background: transparent !important;
}

.custom-menu .el-menu-item {
  background: transparent !important;
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
  height: 64px;
  line-height: 64px;
  border-bottom: 2px solid transparent !important; /* 隐藏默认下划线，改用自定义 */
  transition: all 0.3s ease;
}

.custom-menu .el-menu-item:hover {
  color: var(--color-primary) !important;
  background-color: rgba(92, 124, 138, 0.05) !important; /* 极淡的品牌色背景 */
}

.custom-menu .el-menu-item.is-active {
  color: var(--color-primary) !important;
  font-weight: 700;
  /* 可以选择加一个小的指示点，比大横线更精致 */
  position: relative;
}
.custom-menu .el-menu-item.is-active::after {
  content: '';
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: var(--color-primary);
  border-radius: 50%;
}

/* 用户区域 */
.user-area {
  min-width: 150px;
  display: flex;
  justify-content: flex-end;
}

.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-profile-trigger:hover {
  background: rgba(0,0,0,0.03);
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  max-width: 100px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.auth-buttons .login-btn {
  color: var(--text-secondary);
  margin-right: 10px;
}
.auth-buttons .login-btn:hover {
  color: var(--color-primary);
}

/* =========================================
   主体内容
   ========================================= */
.main-content {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px; /* 给予内部边距 */
  box-sizing: border-box;
}

/* =========================================
   页脚
   ========================================= */
.app-footer {
  text-align: center;
  padding: 30px 0;
  color: var(--text-light);
  font-size: 13px;
  border-top: 1px solid rgba(0,0,0,0.03);
  margin-top: 40px;
}
</style>