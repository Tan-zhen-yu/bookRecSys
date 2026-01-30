<template>
  <div class="category-page-container">
    <div class="category-layout">
      
      <!-- 左侧：分类导航 (Side Card 风格) -->
      <aside class="category-sidebar side-card">
        <div class="sidebar-header">
          <i class="el-icon-collection"></i>
          <span>图书分类</span>
        </div>
        <el-menu
          :default-active="activeId"
          @select="handleSelect"
          class="custom-category-menu"
        >
          <el-menu-item index="0">
            <i class="el-icon-menu"></i>
            <span slot="title">全部图书</span>
          </el-menu-item>
          <el-menu-item v-for="item in categories" :key="item.id" :index="item.id.toString()">
            <i class="el-icon-reading"></i>
            <span slot="title">{{ item.name }}</span>
          </el-menu-item>
        </el-menu>
      </aside>
      
      <!-- 右侧：图书内容 (Main Panel 风格) -->
      <main class="category-main main-panel">
        <header class="content-header">
          <div class="category-info">
            <h2 class="current-cat-name">{{ currentCategoryName }}</h2>
            <span class="book-count">共 {{ total }} 本图书</span>
          </div>
        </header>
        
        <!-- 图书网格 -->
        <div class="books-grid" v-if="tableData.length > 0">
          <div 
            class="shelf-item" 
            v-for="book in tableData" 
            :key="book.id"
            @click="$router.push('/book/' + book.id)"
          >
            <div class="cover-wrapper">
              <img :src="book.coverUrl" class="book-cover">
              <div class="hover-mask">
                <el-button type="primary" size="small" round ripple>查看详情</el-button>
              </div>
            </div>
            <div class="book-info">
              <div class="book-title">{{ book.title }}</div>
              <div class="book-author">{{ book.author }}</div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-empty description="该分类下暂无图书"></el-empty>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="pageNum"
            @current-change="handlePageChange">
          </el-pagination>
        </div>
      </main>
      
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
      categories: [],
      tableData: [],
      activeId: '0',
      total: 0,
      pageNum: 1,
      pageSize: 12
    }
  },
  computed: {
    currentCategoryName() {
      if (this.activeId === '0') return "全部图书";
      let cat = this.categories.find(c => c.id.toString() === this.activeId);
      return cat ? cat.name : "未知分类";
    }
  },
  created() {
    this.loadCategories();
    this.loadBooks();
  },
  methods: {
    loadCategories() {
      request.get("/category/list").then(res => this.categories = res.data);
    },
    loadBooks() {
      request.get("/book/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          categoryId: this.activeId === '0' ? null : this.activeId
        }
      }).then(res => {
        this.tableData = res.data.records;
        this.total = res.data.total;
      })
    },
    handleSelect(index) {
      this.activeId = index;
      this.pageNum = 1;
      this.loadBooks();
    },
    handlePageChange(page) {
      this.pageNum = page;
      this.loadBooks();
      // 平滑滚动回顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
</script>

<style scoped>
/* ================= 变量定义 (可根据全局调整) ================= */
:root {
  --color-primary: #409EFF;
  --text-main: #2d3436;
  --text-secondary: #636e72;
  --text-light: #b2bec3;
  --radius-md: 12px;
  --radius-sm: 8px;
  --shadow-sm: 0 4px 12px rgba(0,0,0,0.05);
  --shadow-hover: 0 12px 24px rgba(0,0,0,0.12);
}

/* ================= 基础布局 ================= */
.category-page-container {
  background-color: #f8f9fa; /* 极淡的底色增加呼吸感 */
  padding: 40px 0;
  min-height: 100vh;
}

.category-layout {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ================= 左侧菜单 ================= */
.category-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: white;
  padding: 20px 0;
  position: sticky;
  top: 20px;
}

.sidebar-header {
  padding: 0 25px 15px;
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 10px;
}

/* 重塑 Element Menu 样式 */
.custom-category-menu {
  border-right: none !important;
}

.custom-category-menu >>> .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  transition: all 0.3s;
}

.custom-category-menu >>> .el-menu-item:hover {
  background-color: #f5f7fa !important;
  color: var(--color-primary);
}

.custom-category-menu >>> .el-menu-item.is-active {
  background-color: #ecf5ff !important;
  color: var(--color-primary);
  font-weight: 600;
}

/* ================= 右侧内容区 ================= */
.category-main {
  flex: 1;
  background: white;
  min-height: 700px;
  padding: 30px;
}

.content-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 20px;
}

.current-cat-name {
  font-size: 1.6rem;
  margin: 0;
  color: var(--text-main);
  font-weight: 600;
}

.book-count {
  font-size: 0.9rem;
  color: var(--text-light);
  margin-top: 5px;
  display: block;
}

/* ================= 图书网格 (核心改动) ================= */
.books-grid {
  display: grid;
  /* 关键改动 1：使用 minmax(0, 1fr) 替代 1fr */
  /* 这会强制网格单元格忽略内部图片的原始宽度，必须乖乖缩放 */
  grid-template-columns: repeat(4, minmax(0, 1fr)); 
  gap: 30px 20px;
  grid-auto-rows: min-content; 
  animation: fadeIn 0.6s ease;
}
.shelf-item {
  cursor: pointer;
  transition: transform 0.3s;
  width: 100%;
  /* 关键改动 2：防止垂直方向溢出 */
  overflow: hidden; 
}
.cover-wrapper {
  position: relative;
  width: 100%;
  /* 依然保持 3/4 比例 */
  aspect-ratio: 3 / 4; 
  border-radius: var(--radius-sm);
  background: #f5f5f5;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  
  /* 关键改动 3：双重保险，强制隐藏超出比例的部分 */
  overflow: hidden; 
  display: block; /* 确保它是一个块级盒子 */
}
.book-cover {
  /* 关键改动 4：强制撑满容器，不管原图多大 */
  width: 100% !important;
  height: 100% !important;
  
  /* 核心：等比例缩放并填充，不拉伸 */
  object-fit: cover; 
  
  /* 消除图片底部的空白 */
  display: block; 
  transition: transform 0.5s ease;
}
.hover-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 2; /* 确保在最上层 */
  backdrop-filter: blur(2px);
}

/* 悬浮交互 */
.shelf-item:hover {
  transform: translateY(-6px);
}

.shelf-item:hover .book-cover {
  transform: scale(1.08);
}

.shelf-item:hover .hover-mask {
  opacity: 1;
}

.shelf-item:hover .cover-wrapper {
  box-shadow: var(--shadow-hover);
}

/* 图书文字信息 */
.book-info {
  margin-top: 15px;
  text-align: left; /* 改为左对齐更显高级 */
}

.book-title {
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--text-main);
  margin-top: 12px;
  
  /* 强制单行，超出显示省略号 */
  display: block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ================= 分页样式优化 ================= */
.pagination-wrapper {
  margin-top: 50px;
  display: flex;
  justify-content: center;
}

/* 穿透修改 Element Pagination */
.pagination-wrapper >>> .el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: var(--color-primary);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.pagination-wrapper >>> .el-pagination.is-background .el-pager li {
  background-color: white;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

/* ================= 动画与通用类 ================= */
.side-card {
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.main-panel {
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 操作按钮容器，确保垂直堆叠并对齐 */
.action-buttons {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center; /* 居中对齐 */
  gap: 12px; /* 按钮之间的间距 */
  margin-bottom: 24px;
}

/* 立即阅读按钮 */
.action-buttons .btn-read {
  width: 100% !important;
  margin: 0 !important; /* 彻底清除偏移 */
  padding: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
}

/* 收藏按钮 */
.action-buttons .btn-collect {
  width: 100% !important;
  margin: 0 !important; /* 彻底清除偏移 */
  padding: 12px;
  font-weight: 600;
}

/* 适配小屏幕 */
/* 适配小屏幕 */
@media (max-width: 1000px) {
  /* 同样使用 minmax(0, 1fr) */
  .books-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
</style>