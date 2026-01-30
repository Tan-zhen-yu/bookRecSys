<template>
  <div id="app">
    <!-- 导航栏可以保持全宽 -->
    <!-- <NavBar class="glass-nav" /> -->
    
    <main class="main-layout">
      <transition name="fade-slide" mode="out-in">
        <router-view />
      </transition>
    </main>
  </div>
</template>

<style>
/* =========================================
   1. Design Token System (调色盘优化)
   ========================================= */
:root {
  /* 品牌色：稍微加深，增加存在感 */
  --color-primary: #4a6775;      
  --color-primary-light: #dbe4e9; 
  --color-accent: #d48a6a;        

  /* 背景色：【关键修改】从 #f8f9fb 改为更沉稳的灰蓝色，减少屏幕反光 */
  --bg-body: #e5e9ed;             /* 深一点的底色，消除“太白”的感觉 */
  --bg-card: #fdfdfd;             /* 卡片不再是纯白，带一点暖调 */
  
  /* 文字色 */
  --text-main: #2c3e50;           
  --text-secondary: #50616d;      
  --text-light: #7f8c8d;          

  /* 间距与圆角 */
  --spacing-xs: 8px;
  --spacing-sm: 16px;
  --spacing-md: 24px;
  --spacing-lg: 40px;
  --radius-md: 12px;
  --radius-lg: 20px;

  /* 阴影：配合深色背景，阴影可以稍微明显一点点 */
  --shadow-sm: 0 4px 12px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.08);
  --shadow-hover: 0 15px 35px rgba(74, 103, 117, 0.12);
}

/* =========================================
   2. Global Reset
   ========================================= */
body {
  margin: 0;
  padding: 0;
  background-color: var(--bg-body);
  /* 使用渐变背景能让视觉更丰富，不显单调 */
  background-image: linear-gradient(135deg, #e5e9ed 0%, #dce2e8 100%);
  color: var(--text-main);
  font-family: 'Inter', -apple-system, sans-serif;
  -webkit-font-smoothing: antialiased;
  line-height: 1.6;
}

/* =========================================
   3. Layout (解决留白问题)
   ========================================= */
#app {
  min-height: 100vh;
}

.main-layout {
  width: 100%;           /* 宽度占满 */
  max-width: 100%;       /* 【关键】移除 1200px 限制 */
  margin: 0;             /* 【关键】移除居中外边距 */
  padding: var(--spacing-md); /* 仅保留必要的内边距防止贴边 */
  box-sizing: border-box;
  min-height: calc(100vh - 60px); /* 减去可能存在的导航栏高度 */
}

/* 卡片样式：增加稍微明显的边界线，在不那么白的背景上更有层次 */
.card-breathing {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: var(--spacing-md);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.6); /* 亮色描边增加高级感 */
}

.card-breathing:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
  background: #ffffff; /* 悬浮时变亮，产生交互感 */
}

/* =========================================
   4. 滚动条美化（配合深色调）
   ========================================= */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: var(--bg-body);
}
::-webkit-scrollbar-thumb {
  background: #bdc3c7;
  border-radius: 10px;
  border: 2px solid var(--bg-body); /* 这里的边框能制造“悬浮”滚动条效果 */
}
::-webkit-scrollbar-thumb:hover {
  background: var(--color-primary);
}

/* =========================================
   5. 动画效果
   ========================================= */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}

.fade-slide-enter {
  opacity: 0;
  transform: scale(0.98); /* 稍微缩小进入，比位移更有呼吸感 */
}

.fade-slide-leave-to {
  opacity: 0;
}
/* 在 App.vue 的 <style> 末尾添加 */
.logo-icon, .logo-text {
  background-color: transparent !important;
}
/* src/App.vue */

/* 夜间模式下的抽屉样式覆盖 */
.theme-dark .el-drawer {
  background-color: #2c2c2c !important;
  color: #b0b0b0 !important;
}
.theme-dark .el-drawer__header {
  color: #b0b0b0 !important;
}
.theme-dark .catalog-item {
  color: #b0b0b0 !important;
  border-bottom-color: #333 !important;
}
.theme-dark .catalog-item:hover {
  background-color: #333 !important;
}
</style>