<template>
  <div class="reader-app" :class="currentTheme" @mousemove="handleMouseMove">
    
    <!-- 1. 顶部沉浸式菜单 -->
    <transition name="slide-down">
      <div v-show="menuVisible" class="reader-header glass-bar">
        <div class="header-left" @click="$router.go(-1)">
          <i class="el-icon-arrow-left"></i>
          <span class="back-text">返回</span>
        </div>
        <div class="header-title">{{ bookTitle }}</div>
        <div class="header-right" @click="$router.push('/')">
          <i class="el-icon-house"></i>
        </div>
      </div>
    </transition>

    <!-- 2. 核心阅读区域 -->
    <!-- 点击中间区域唤起菜单，点击两侧翻页(可选，这里主要做点击唤起) -->
    <div class="reader-container" @click="toggleMenu">
      <div class="paper-area" :style="{ fontSize: fontSize + 'px' }">
        
        <!-- 标题 -->
        <h2 class="chapter-title">{{ chapter.title }}</h2>
        <div class="chapter-meta" v-if="chapter.title">本章字数：{{ chapter.content ? chapter.content.length : 0 }}</div>

        <!-- 正文 -->
        <el-skeleton v-if="loading" :rows="15" animated class="mt-4" />
        
        <div v-else class="text-body" v-html="formatContent(chapter.content)"></div>

        <!-- 底部翻页区 -->
        <div class="pagination-area" v-if="!loading">
          <button 
            class="nav-btn prev" 
            :disabled="!chapter.prevId" 
            @click.stop="loadChapter(chapter.prevId)">
            上一章
          </button>
          
          <button class="nav-btn menu" @click.stop="showCatalog = true">
            <i class="el-icon-s-fold"></i> 目录
          </button>
          
          <button 
            class="nav-btn next" 
            :disabled="!chapter.nextId" 
            @click.stop="loadChapter(chapter.nextId)">
            下一章
          </button>
        </div>
      </div>
    </div>

    <!-- 3. 底部控制栏 -->
    <transition name="slide-up">
      <div v-show="menuVisible" class="reader-footer glass-bar">
        <div class="setting-group">
          <span class="setting-label"><i class="el-icon-zoom-in"></i> 字号</span>
          <el-slider v-model="fontSize" :min="14" :max="28" :step="2" :show-tooltip="false" class="custom-slider"></el-slider>
        </div>
        
        <div class="setting-group">
          <span class="setting-label"><i class="el-icon-sunny"></i> 模式</span>
          <div class="theme-list">
            <div class="theme-item light" @click="setTheme('theme-light')" :class="{active: currentTheme==='theme-light'}"></div>
            <div class="theme-item eye" @click="setTheme('theme-eye')" :class="{active: currentTheme==='theme-eye'}"></div>
            <div class="theme-item dark" @click="setTheme('theme-dark')" :class="{active: currentTheme==='theme-dark'}"></div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 4. 侧边目录抽屉 -->
    <el-drawer
      :visible.sync="showCatalog"
      direction="ltr"
      size="320px"
      :with-header="false"
      custom-class="catalog-drawer">
      
      <div class="catalog-header">
        <h3>目录</h3>
        <span class="catalog-count">共 {{ catalogList.length }} 章</span>
      </div>
      
      <div class="catalog-body">
        <div 
          v-for="item in catalogList" 
          :key="item.id" 
          class="catalog-item"
          :class="{ active: currentChapterId === item.id }"
          @click="handleChapterClick(item.id)">
          <span class="dot"></span>
          <span class="chapter-name">{{ item.title }}</span>
        </div>
      </div>
    </el-drawer>

  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Read",
  data() {
    return {
      bookId: this.$route.params.bookId,
      bookTitle: '',
      loading: false,
      menuVisible: true, // 初始显示菜单
      showCatalog: false,
      
      // 设置
      fontSize: 18,
      currentTheme: 'theme-eye', 
      
      currentChapterId: null,
      catalogList: [],
      chapter: {
        title: '',
        content: '',
        prevId: null,
        nextId: null
      },
      
      // 自动隐藏菜单的计时器
      menuTimer: null
    }
  },
  created() {
    if (!this.bookId) {
      this.$message.error("参数错误");
      return;
    }
    this.loadCatalog();
    
    // 3秒后自动隐藏菜单，进入沉浸模式
    this.resetMenuTimer();
  },
  methods: {
    loadCatalog() {
      // 使用暴力拼接法，确保不出错
      request.get("/read/catalog?bookId=" + this.bookId).then(res => {
        if (res.code == 200) {
          this.catalogList = res.data.chapters;
          this.bookTitle = res.data.bookTitle;
          
          const startId = this.catalogList.length > 0 ? this.catalogList[0].id : null;
          if (startId) {
            this.loadChapter(startId);
          }
        }
      })
    },
    loadChapter(chapterId) {
      if (!chapterId) return;
      this.loading = true;
      this.currentChapterId = chapterId;
      window.scrollTo({ top: 0, behavior: 'smooth' });

      request.get("/read/chapter?chapterId=" + chapterId).then(res => {
        if (res.code == 200) {
          this.chapter = res.data;
        }
      }).finally(() => {
        this.loading = false;
        // 翻页后隐藏菜单
        this.menuVisible = false;
      });
    },
    handleChapterClick(id) {
      this.showCatalog = false;
      this.loadChapter(id);
    },
    toggleMenu() {
      this.menuVisible = !this.menuVisible;
    },
    setTheme(theme) {
      this.currentTheme = theme;
    },
    handleMouseMove(e) {
      // 鼠标移动到顶部或底部时唤起菜单 (可选优化)
      if (e.clientY < 50 || e.clientY > window.innerHeight - 50) {
        this.menuVisible = true;
      }
    },
    resetMenuTimer() {
      if (this.menuTimer) clearTimeout(this.menuTimer);
      this.menuTimer = setTimeout(() => {
        this.menuVisible = false;
      }, 3000);
    },
    formatContent(text) {
      if (!text) return '';
      // 处理换行，并且给首行缩进
      let pTags = text.split(/\n+/).map(p => `<p>${p.trim()}</p>`).join('');
      return pTags;
    }
  },
  beforeDestroy() {
    if (this.menuTimer) clearTimeout(this.menuTimer);
  }
}
</script>

<style scoped>
/* 引入谷歌字体 (宋体风格)，让文字更好看 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700&display=swap');

/* ================= CSS 变量定义 (换肤核心) ================= */
.theme-light {
  --bg-color: #ffffff;
  --text-main: #2c3e50;
  --text-sub: #7f8c8d;
  --accent: #3498db;
  --bar-bg: rgba(255, 255, 255, 0.95);
  --shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.theme-eye {
  --bg-color: #f6f1e3; /* 羊皮纸色/米色 */
  --text-main: #5d4037; /* 深褐 */
  --text-sub: #8d6e63;
  --accent: #795548;
  --bar-bg: rgba(246, 241, 227, 0.95);
  --shadow: 0 4px 20px rgba(93, 64, 55, 0.1);
}

.theme-dark {
  --bg-color: #1a1a1a;
  --text-main: #b0b0b0;
  --text-sub: #666;
  --accent: #d35400;
  --bar-bg: rgba(26, 26, 26, 0.95);
  --shadow: 0 4px 20px rgba(0,0,0,0.5);
}

/* ================= 基础容器 ================= */
.reader-app {
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-main);
  transition: all 0.4s ease; /* 换肤过渡 */
  font-family: 'Noto Serif SC', 'Songti SC', serif; /* 使用衬线体，阅读感极强 */
}

/* ================= 顶部/底部 玻璃栏 ================= */
.glass-bar {
  background: var(--bar-bg);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow);
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.reader-header {
  top: 0;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
}

.header-left, .header-right {
  font-size: 20px;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.3s;
  display: flex; align-items: center; gap: 5px;
}
.header-left:hover, .header-right:hover { opacity: 1; color: var(--accent); }
.back-text { font-size: 14px; font-family: sans-serif; }

.header-title {
  font-size: 14px;
  font-weight: bold;
  opacity: 0.8;
  font-family: sans-serif;
}

.reader-footer {
  bottom: 0;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 20%;
}

/* ================= 核心阅读区 (呼吸感关键) ================= */
.reader-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 100px 40px 150px; /* 上下留大白 */
  min-height: 100vh;
  cursor: text; /* 鼠标样式 */
}

.paper-area {
  margin: 0 auto;
  max-width: 100%;
}

.chapter-title {
  font-size: 2.2em;
  margin-bottom: 10px;
  font-weight: 700;
  letter-spacing: 2px;
}

.chapter-meta {
  font-size: 12px;
  color: var(--text-sub);
  margin-bottom: 60px;
  font-family: sans-serif;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  padding-bottom: 20px;
}

/* 正文样式精修 */
.text-body >>> p {
  margin-bottom: 24px; /* 段落间距 */
  line-height: 1.8;    /* 黄金行高 */
  text-align: justify; /* 两端对齐 */
  text-indent: 2em;    /* 首行缩进 */
  letter-spacing: 0.5px;
}

/* ================= 翻页按钮组 ================= */
.pagination-area {
  margin-top: 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-btn {
  background: transparent;
  border: 1px solid var(--text-sub);
  color: var(--text-main);
  padding: 10px 30px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  font-family: sans-serif;
  font-size: 14px;
}

.nav-btn:hover:not(:disabled) {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.nav-btn.menu {
  border: none;
  font-weight: bold;
}

/* ================= 设置面板细节 ================= */
.setting-group {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.setting-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-sub);
  font-family: sans-serif;
  display: flex; align-items: center; gap: 5px;
}

/* 主题球 */
.theme-list { display: flex; gap: 20px; }
.theme-item {
  width: 36px; height: 36px; border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}
.theme-item:hover { transform: scale(1.1); }
.theme-item.active { border-color: var(--accent); transform: scale(1.1); }

.theme-item.light { background: #ffffff; }
.theme-item.eye { background: #f6f1e3; }
.theme-item.dark { background: #333333; }

/* 穿透修改 Slider */
.custom-slider { width: 300px; }
.reader-footer >>> .el-slider__runway { background-color: rgba(0,0,0,0.1); }
.reader-footer >>> .el-slider__bar { background-color: var(--accent); }
.reader-footer >>> .el-slider__button { border-color: var(--accent); }

/* ================= 目录抽屉样式覆盖 ================= */
.catalog-drawer {
  /* 这里的样式可能需要写到全局或者用 /deep/ */
}
.catalog-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: flex-end;
}
.catalog-header h3 { margin: 0; font-size: 18px; color: #333; }
.catalog-count { font-size: 12px; color: #999; }

.catalog-body {
  overflow-y: auto;
  height: calc(100vh - 70px);
}

.catalog-item {
  padding: 15px 20px;
  cursor: pointer;
  transition: background 0.2s;
  display: flex; align-items: center;
  font-size: 14px;
  color: #555;
  border-bottom: 1px dashed #f5f5f5;
}

.catalog-item:hover { background: #f9f9f9; color: #409EFF; }
.catalog-item.active { color: #409EFF; font-weight: bold; background: #e6f7ff; }
.catalog-item .dot {
  width: 6px; height: 6px; background: #ddd; border-radius: 50%; margin-right: 15px;
}
.catalog-item.active .dot { background: #409EFF; }

/* ================= 动画 ================= */
.slide-down-enter-active, .slide-down-leave-active,
.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1), opacity 0.4s;
}
.slide-down-enter, .slide-down-leave-to { transform: translateY(-100%); opacity: 0; }
.slide-up-enter, .slide-up-leave-to { transform: translateY(100%); opacity: 0; }
</style>