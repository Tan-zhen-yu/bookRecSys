<template>
  <div class="rank-container">
    
    <div class="page-header">
      <h1><span class="icon">🏆</span> 热门风向标</h1>
      <p class="subtitle">探索最受好评、最热门以及最新上架的图书</p>
    </div>

    <el-row :gutter="30">
      
      <!-- 1. 高分榜 (Rating) -->
      <el-col :xs="24" :sm="24" :md="8" class="rank-column">
        <div class="rank-card glass-panel" style="--accent-color: #f1c40f;">
          <div class="rank-header">
            <div class="header-icon"><i class="el-icon-trophy"></i></div>
            <div class="header-text">
              <h3>高分必读</h3>
              <span>口碑炸裂神作</span>
            </div>
          </div>
          
          <div class="rank-list">
            <!-- 前3名：图文展示 -->
            <div 
              v-for="(book, index) in ratingRank.slice(0, 3)" 
              :key="'r-'+book.id" 
              class="rank-item top-item"
              @click="goDetail(book.id)">
              <div class="rank-badge top">{{ index + 1 }}</div>
              <div class="book-cover-wrapper">
                <img :src="book.coverUrl || 'https://via.placeholder.com/100x140'" class="cover-img">
              </div>
              <div class="item-info">
                <div class="item-title" :title="book.title">{{ book.title }}</div>
                <div class="item-sub">评分 <span class="score">{{ book.ratingAvg }}</span></div>
              </div>
            </div>

            <!-- 4-10名：文字列表 -->
            <div 
              v-for="(book, index) in ratingRank.slice(3)" 
              :key="'r-'+book.id" 
              class="rank-item normal-item"
              @click="goDetail(book.id)">
              <div class="rank-badge">{{ index + 4 }}</div>
              <div class="item-title-row">
                <span class="text">{{ book.title }}</span>
                <span class="score-sm">{{ book.ratingAvg }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 2. 热度榜 (Hot) -->
      <el-col :xs="24" :sm="24" :md="8" class="rank-column">
        <div class="rank-card glass-panel" style="--accent-color: #ff7675;">
          <div class="rank-header">
            <div class="header-icon"><i class="el-icon-data-line"></i></div>
            <div class="header-text">
              <h3>全站热榜</h3>
              <span>大家都在看</span>
            </div>
          </div>
          
          <div class="rank-list">
            <div 
              v-for="(book, index) in hotRank.slice(0, 3)" 
              :key="'h-'+book.id" 
              class="rank-item top-item"
              @click="goDetail(book.id)">
              <div class="rank-badge top">{{ index + 1 }}</div>
              <div class="book-cover-wrapper">
                <img :src="book.coverUrl || 'https://via.placeholder.com/100x140'" class="cover-img">
              </div>
              <div class="item-info">
                <div class="item-title" :title="book.title">{{ book.title }}</div>
                <div class="item-sub">{{ book.ratingCount }} 人热议</div>
              </div>
            </div>

            <div 
              v-for="(book, index) in hotRank.slice(3)" 
              :key="'h-'+book.id" 
              class="rank-item normal-item"
              @click="goDetail(book.id)">
              <div class="rank-badge">{{ index + 4 }}</div>
              <div class="item-title-row">
                <span class="text">{{ book.title }}</span>
                <span class="hot-val"><i class="el-icon-fire"></i> {{ book.ratingCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 3. 新书榜 (New) -->
      <el-col :xs="24" :sm="24" :md="8" class="rank-column">
        <div class="rank-card glass-panel" style="--accent-color: #74b9ff;">
          <div class="rank-header">
            <div class="header-icon"><i class="el-icon-reading"></i></div>
            <div class="header-text">
              <h3>新书速递</h3>
              <span>发现新鲜好书</span>
            </div>
          </div>
          
          <div class="rank-list">
            <div 
              v-for="(book, index) in newRank.slice(0, 3)" 
              :key="'n-'+book.id" 
              class="rank-item top-item"
              @click="goDetail(book.id)">
              <div class="rank-badge top">{{ index + 1 }}</div>
              <div class="book-cover-wrapper">
                <img :src="book.coverUrl || 'https://via.placeholder.com/100x140'" class="cover-img">
              </div>
              <div class="item-info">
                <div class="item-title" :title="book.title">{{ book.title }}</div>
                <div class="item-sub">{{ book.author }}</div>
              </div>
            </div>

            <div 
              v-for="(book, index) in newRank.slice(3)" 
              :key="'n-'+book.id" 
              class="rank-item normal-item"
              @click="goDetail(book.id)">
              <div class="rank-badge">{{ index + 4 }}</div>
              <div class="item-title-row">
                <span class="text">{{ book.title }}</span>
                <span class="date-val">{{ formatDate(book.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

    </el-row>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Rank",
  data() {
    return {
      ratingRank: [], // 高分
      hotRank: [],    // 热门
      newRank: []     // 新书
    }
  },
  created() {
    this.loadRanks();
  },
  methods: {
    loadRanks() {
      // 并发请求，提高加载速度
      Promise.all([
        request.get("/book/rank/rating"),
        request.get("/book/rank/hot"),
        request.get("/book/rank/new")
      ]).then(results => {
        // 假设后端返回的数据结构一致，如果不一致需分别处理
        if(results[0].code == 200) this.ratingRank = results[0].data;
        if(results[1].code == 200) this.hotRank = results[1].data;
        if(results[2].code == 200) this.newRank = results[2].data;
      });
    },
    goDetail(id) {
      this.$router.push("/book/" + id);
    },
    formatDate(dateStr) {
      if(!dateStr) return '';
      // 简单截取日期部分，假设格式为 YYYY-MM-DD HH:mm:ss
      return dateStr.split(' ')[0];
    }
  }
}
</script>

<style scoped>
.rank-container {
  padding-bottom: 40px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeDown 0.6s ease;
}
.page-header h1 {
  font-size: 2rem;
  color: var(--text-main);
  margin-bottom: 10px;
}
.page-header .icon {
  display: inline-block;
  animation: float 3s ease-in-out infinite;
}
.page-header .subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
}

/* 响应式调整间距 */
.rank-column {
  margin-bottom: 30px;
}

.rank-card {
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  height: 100%;
  transition: transform 0.3s;
  /* 默认强调色，会被内联样式覆盖 */
  --accent-color: #5c7c8a; 
}

.rank-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

/* 头部设计 */
.rank-header {
  padding: 24px;
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(240,240,240,0.4));
  border-bottom: 1px solid rgba(0,0,0,0.03);
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-icon {
  width: 48px; height: 48px;
  background: var(--accent-color);
  color: white;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.header-text h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-main);
}
.header-text span {
  font-size: 0.8rem;
  color: var(--text-light);
}

/* 列表区域 */
.rank-list {
  padding: 10px 20px 24px;
}

.rank-item {
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 8px;
}
.rank-item:hover {
  background: rgba(0,0,0,0.02);
}

/* Top 3 样式 */
.top-item {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  margin-bottom: 12px;
  position: relative;
}

.rank-badge {
  font-weight: 800;
  font-size: 1rem;
  color: var(--text-light);
  width: 30px;
  text-align: center;
  margin-right: 12px;
}
.rank-badge.top {
  font-size: 1.5rem;
  color: var(--accent-color); /* 使用当前卡片的强调色 */
  font-style: italic;
}

.book-cover-wrapper {
  width: 48px;
  height: 68px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  margin-right: 15px;
  flex-shrink: 0;
}
.cover-img {
  width: 100%; height: 100%; object-fit: cover;
}

.item-info { flex: 1; overflow: hidden; }
.item-title {
  font-weight: 600;
  color: var(--text-main);
  font-size: 0.95rem;
  margin-bottom: 4px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.item-sub { font-size: 0.8rem; color: var(--text-secondary); }
.score { color: #f1c40f; font-weight: bold; }

/* 4-10名 普通样式 */
.normal-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  font-size: 0.9rem;
}
.normal-item .rank-badge {
  font-size: 0.9rem;
  font-weight: bold;
  color: #999;
}
.item-title-row {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  overflow: hidden;
}
.item-title-row .text {
  flex: 1;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  color: var(--text-main);
  margin-right: 10px;
}
.hot-val { color: #ff7675; font-size: 0.8rem; }
.date-val { color: #ccc; font-size: 0.8rem; }
.score-sm { color: #f1c40f; font-weight: bold; font-size: 0.85rem; }

@keyframes fadeDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}
</style>