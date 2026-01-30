<template>
  <div class="home-container">
    
    <!-- 1. 顶部 Hero 区域：搜索与筛选 -->
    <div class="hero-section">
      <h1 class="page-title">探索你的下一本好书</h1>
      <div class="search-bar-wrapper glass-panel">
        <el-input 
          v-model="keyword" 
          placeholder="搜索书名 / 作者..." 
          class="custom-input"
          @keyup.enter.native="loadBooks"
          clearable>
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
        
        <el-select v-model="categoryId" placeholder="全部分类" class="custom-select" @change="loadBooks" clearable>
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
        
        <el-button type="primary" class="search-btn" @click="loadBooks">探索</el-button>
      </div>
    </div>

    <!-- 2. 精选与推荐区 (有数据时才显示) -->
    <div v-if="bannerList.length > 0 || (user.id && recommendBooks.length > 0)" class="section-wrapper">
      
      <!-- 轮播图：高分榜 -->
      <div v-if="bannerList.length > 0" class="margin-bottom-lg">
        <h2 class="section-title"><i class="el-icon-trophy"></i> 高分必读</h2>
        <el-carousel :interval="5000" type="card" height="280px" indicator-position="none">
          <el-carousel-item v-for="item in bannerList" :key="item.id">
            <div class="banner-card" @click="goDetail(item.id)">
              <div class="banner-img-wrapper">
                <img :src="item.coverUrl" alt="cover">
                <div class="banner-overlay">
                  <div class="banner-content">
                    <h3>{{ item.title }}</h3>
                    <p>{{ item.author }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 猜你喜欢 -->
      <div v-if="user.id && recommendBooks.length > 0" class="margin-bottom-lg">
        <h2 class="section-title"><i class="el-icon-magic-stick"></i> 猜你喜欢</h2>
        <el-row :gutter="24">
          <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="book in recommendBooks" :key="book.id" class="margin-bottom-md">
            <!-- 复用呼吸感卡片样式 -->
            <div class="book-card card-breathing" @click="goDetail(book.id)">
              <div class="card-image-box">
                <div v-if="book.matchScore" class="match-badge" :style="{ backgroundColor: getMatchColor(book.matchScore) }">
                  {{ book.matchScore }}%
                </div>
                <img :src="book.coverUrl || 'https://via.placeholder.com/150x200'" loading="lazy">
              </div>
              <div class="card-info simple">
                <div class="book-title" :title="book.title">{{ book.title }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 3. 主图书列表 -->
    <div class="section-wrapper">
      <h2 class="section-title">
        <span v-if="!categoryId && !keyword">所有图书</span>
        <span v-else>搜索结果</span>
      </h2>
      
      <el-row :gutter="30">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="book in tableData" :key="book.id" class="margin-bottom-md">
          <div class="book-card card-breathing" @click="goDetail(book.id)">
            <!-- 封面图 -->
            <div class="card-image-box">
              <div v-if="book.matchScore" class="match-badge" :style="{ backgroundColor: getMatchColor(book.matchScore) }">
                {{ book.matchScore }}%
              </div>
              <img :src="book.coverUrl || 'https://via.placeholder.com/150x200?text=No+Cover'" loading="lazy">
            </div>
            
            <!-- 信息区 -->
            <div class="card-info">
              <div class="book-title" :title="book.title">{{ book.title }}</div>
              <div class="book-meta">
                <span class="book-author">{{ book.author }}</span>
              </div>
              <div class="book-rating">
                <i class="el-icon-star-on active"></i>
                <span>{{ book.ratingAvg || '暂无' }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="tableData.length === 0" description="这里空空如也，换个词试试？"></el-empty>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="total">
        </el-pagination>
      </div>
    </div>

  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Home",
  data() {
    return {
      tableData: [],
      categories: [],
      pageNum: 1,
      pageSize: 12, // 稍微增加每页数量，填满网格
      total: 0,
      keyword: '',
      categoryId: null,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      bannerList: [],
      recommendBooks: []
    }
  },
  created() {
    this.loadCategories();
    this.loadBooks();
    this.loadBanner();
    if (this.user.id) {
      this.loadRecommend();
    }
  },
  methods: {
    getMatchColor(score) {
      if (score >= 90) return '#ff7675'; // 柔和红
      if (score >= 80) return '#fab1a0'; // 柔和橙
      return '#74b9ff';                  // 柔和蓝
    },
    loadBanner() {
      request.get("/book/rank/rating").then(res => {
        if (res.code == 200) {
          this.bannerList = res.data.slice(0, 5);
        }
      })
    },
    loadCategories() {
      request.get("/category/list").then(res => {
        if(res.code == 200) this.categories = res.data;
      })
    },
    loadBooks() {
      request.get("/book/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.keyword,
          categoryId: this.categoryId
        }
      }).then(res => {
        if(res.code == 200) {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      })
    },
   loadRecommend() {
      request.get("/recommend/user", {
        params: { userId: this.user.id }
      }).then(res => {
        if (res.code == 200) {
          // 前端再次截取，确保布局不乱
          this.recommendBooks = res.data.slice(0, 6);
        }
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum;
      this.loadBooks();
      // 翻页后平滑滚动到列表顶部
      window.scrollTo({ top: 400, behavior: 'smooth' });
    },
    goDetail(id) {
      this.$router.push("/book/" + id);
    }
  }
}
</script>

<style scoped>
/* =================================
   布局与间距
   ================================= */
.home-container {
  padding-bottom: 60px;
}

.section-wrapper {
  margin-top: 40px;
  animation: fadeUp 0.8s var(--ease-out-back);
}

.margin-bottom-md { margin-bottom: 24px; }
.margin-bottom-lg { margin-bottom: 40px; }

/* =================================
   Hero Header (搜索区)
   ================================= */
.hero-section {
  text-align: center;
  padding: 40px 0 20px;
  animation: fadeUp 0.6s var(--ease-out-back);
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 30px;
  letter-spacing: -0.5px;
}

.search-bar-wrapper {
  display: inline-flex;
  align-items: center;
  background: white;
  padding: 8px;
  border-radius: 50px; /* 大圆角胶囊状 */
  box-shadow: var(--shadow-md);
  max-width: 700px;
  width: 90%;
}

/* 穿透修改 ElementUI 样式以适应设计 */
.custom-input >>> .el-input__inner {
  border: none;
  background: transparent;
  font-size: 16px;
  padding-left: 35px;
}

.custom-select >>> .el-input__inner {
  border: none;
  background: transparent;
  border-left: 1px solid #eee; /* 分隔线 */
  border-radius: 0;
  width: 120px;
  text-align: center;
}

.search-btn {
  border-radius: 40px;
  padding: 12px 30px;
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  font-weight: 600;
  transition: all 0.3s;
}

.search-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(92, 124, 138, 0.4);
}

/* =================================
   通用标题
   ================================= */
.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title i {
  color: var(--color-accent);
}

/* =================================
   轮播卡片优化
   ================================= */
.banner-card {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  cursor: pointer;
  box-shadow: var(--shadow-md);
}

.banner-img-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.banner-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.banner-card:hover img {
  transform: scale(1.05);
}

.banner-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 60%;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
  display: flex;
  align-items: flex-end;
  padding: 20px;
}

.banner-content {
  color: white;
  text-align: left;
}

.banner-content h3 { margin: 0; font-size: 1.2rem; font-weight: 600; }
.banner-content p { margin: 5px 0 0; font-size: 0.9rem; opacity: 0.9; }

/* =================================
   图书卡片 (核心样式)
   ================================= */
.book-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  cursor: pointer;
  background: var(--bg-card);
  position: relative;
    max-width: 100%;
}

.card-image-box {
  width: 100%;
  height: 0;
  padding-bottom: 140%; /* 保持 1:1.4 的黄金图书比例 */
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  border-radius: var(--radius-sm); /* 图片本身也有微圆角 */
  margin-bottom: 12px;
}

.card-image-box img {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.book-card:hover .card-image-box img {
  transform: scale(1.08);
}

.match-badge {
  position: absolute;
  top: 10px; right: 10px;
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 20px;
  z-index: 5; /* 确保在最上层 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  backdrop-filter: blur(4px);
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.card-info.simple { text-align: center; }

.book-title {
  font-weight: 700;
  color: var(--text-main);
  font-size: 1rem;
  margin-bottom: 6px;
  
  /* 强制一行，超出显示省略号 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  /* 固定高度，防止有些书名短有些长导致对不齐 */
  height: 24px; 
  line-height: 24px;
}

.book-meta {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-rating {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  color: var(--text-light);
}

.book-rating i.active {
  color: #f1c40f; /* 星星黄 */
  margin-right: 4px;
}

/* =================================
   分页居中
   ================================= */
.pagination-wrapper {
  margin-top: 40px;
  text-align: center;
  opacity: 0.9;
}

/* 简单的入场动画 */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>