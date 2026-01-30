<template>
  <div class="page-container">
    <!-- 1. 动态大气背景 -->
    <div class="ambient-bg" :style="{ backgroundImage: `url(${book.coverUrl || ''})` }"></div>
    <div class="ambient-mask"></div>

    <div class="main-content">
      <el-row :gutter="40">
        
        <!-- ================= 左侧：封面与核心操作 (Sticky 固定) ================= -->
        <el-col :span="6" class="sidebar-wrapper">
          <div class="sticky-sidebar">
            <div class="cover-box">
              <img :src="book.coverUrl || 'https://via.placeholder.com/300x400?text=No+Cover'" class="book-cover">
              <div class="cover-shadow"></div>
            </div>

            <div class="action-buttons">
              <el-button 
                type="primary" 
                icon="el-icon-reading" 
                class="btn-read"
                round
                @click="goToReader">
                立即阅读
              </el-button>
              <el-button 
                :type="isCollected ? 'warning' : 'default'" 
                :icon="isCollected ? 'el-icon-star-on' : 'el-icon-star-off'" 
                class="btn-collect"
                round
                @click="toggleCollect">
                {{ isCollected ? '已收藏' : '加入书架' }}
              </el-button>
            </div>

            <!-- 评分概览放在左侧更显眼 -->
            <div class="rating-overview glass-card-sm">
              <div class="score">{{ book.ratingAvg || '0.0' }}</div>
              <el-rate 
                :value="Number(book.ratingAvg)" 
                disabled 
                text-color="#ff9900"
                score-template="{value}">
              </el-rate>
              <div class="count">{{ book.ratingCount || 0 }} 人参与评价</div>
            </div>
          </div>
        </el-col>

        <!-- ================= 右侧：详细内容流 ================= -->
        <el-col :span="18">
          <div class="content-panel glass-panel-lg">
            
            <!-- 标题区 -->
            <div class="header-section">
              <h1 class="book-title">{{ book.title }}</h1>
              <div class="book-meta">
                <span class="meta-item"><i class="el-icon-user"></i> {{ book.author }}</span>
                <span class="meta-divider">/</span>
                <span class="meta-item"><i class="el-icon-office-building"></i> {{ book.publisher }}</span>
                <span class="meta-divider">/</span>
                <el-tag size="small" effect="plain" class="category-tag">
                {{ categories.find(c => c.id === book.categoryId) ? categories.find(c => c.id === book.categoryId).name : '加载中...' }}
              </el-tag>
              </div>
            </div>

            <!-- 简介区 -->
            <div class="section-block">
              <h3 class="section-title">简介</h3>
              <p class="description-text">{{ book.description || '暂无简介...' }}</p>
            </div>

            <!-- AI 词云 -->
            <div class="section-block" v-show="hasKeywords">
              <h3 class="section-title">
                <i class="el-icon-cpu" style="color: var(--color-primary)"></i> AI 核心视点
              </h3>
              <div class="word-cloud-container">
                <div id="wordCloud" class="word-cloud-box"></div>
              </div>
            </div>

            <el-divider class="soft-divider"></el-divider>

            <!-- 关联推荐 -->
            <div class="section-block">
              <h3 class="section-title">喜欢这本书的人也喜欢</h3>
              <div v-if="relatedBooks.length > 0" class="related-grid">
                <div 
                  v-for="item in relatedBooks" 
                  :key="item.id" 
                  class="mini-book-card" 
                  @click="goRelated(item.id)">
                  
                  <div class="mini-cover">
                    <img :src="item.coverUrl || 'https://via.placeholder.com/150x200'">
                    <span class="match-badge" :style="{ background: getMatchColor(item.matchScore) }">
                      {{ item.matchScore }}%
                    </span>
                  </div>
                  <div class="mini-title" :title="item.title">{{ item.title }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无关联推荐" :image-size="60"></el-empty>
            </div>

            <el-divider class="soft-divider"></el-divider>

            <!-- 评论区 -->
          <div class="section-block">
  <h3 class="section-title">书友评论 ({{ comments.length }})</h3>
  
  <div class="comment-input-area">
    <div v-if="user.id" class="user-comment-box">
      <div class="rate-row">
        <span>我的评分：</span>
        <el-rate v-model="form.score" show-text></el-rate>
      </div>
      <el-input 
        type="textarea" 
        :rows="3" 
        placeholder="写下你的真知灼见..." 
        v-model="form.comment"
        resize="none">
      </el-input>
      <div class="action-row">
        <el-button type="primary" size="small" @click="submitRating" round>发布评论</el-button>
      </div>
    </div>
    <div v-else class="login-placeholder">
      <el-button type="primary" plain round @click="$router.push('/login')">登录后参与讨论</el-button>
    </div>
  </div>

  <div class="comments-scroll-wrapper">
    <div class="comments-list">
      <div v-for="item in comments" :key="item.id" class="comment-item">
        <div class="comment-avatar">
          <el-avatar :size="40" :src="item.avatarUrl" style="background: #eef2f3; color: #5c7c8a;">
            {{ item.nickname ? item.nickname.charAt(0) : 'U' }}
          </el-avatar>
        </div>
        <div class="comment-body">
          <div class="comment-header">
            <span class="comment-author">{{ item.nickname || '匿名书友' }}</span>
            <el-rate :value="Number(item.score)" disabled size="small" class="comment-rate"></el-rate>
            <span class="comment-date">{{ item.createTime }}</span>
          </div>
          <p class="comment-text">{{ item.comment }}</p>
        </div>
      </div>
      <div v-if="comments.length > 0" class="no-more-msg">已显示全部评论</div>
      <el-empty v-if="comments.length === 0" description="还没人评论，快来抢沙发"></el-empty>
    </div>
  </div>
</div>

          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import * as echarts from 'echarts';
import 'echarts-wordcloud';

export default {
  name: "BookDetail",
  data() {
    return {
      id: this.$route.params.id,
      book: {},
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      isCollected: false,
      relatedBooks: [],
      form: { score: 0, comment: '' },
      comments: [],
      hasKeywords: false,
      categories: [], // 新增：存储所有分类列表
      chartInstance: null
    }
  },
  created() {
    this.loadAllData();
    if (this.user.id) {
      this.addHistory();
    }
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
    window.removeEventListener('resize', this.resizeChart);
  },
  watch: {
    '$route.params.id': function(val) {
      this.id = val;
      this.loadAllData();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  },
  methods: {
    loadAllData() {
      this.loadDetail();
      this.loadComments();
      this.checkCollection();
      this.loadRelated();
      this.loadCategories(); // <-- 记得加上这行
    },
    goToReader() {
  this.$router.push('/read/' + this.id);
},
    loadCategories() {
    request.get("/category/list").then(res => {
      if (res.code == 200) {
        this.categories = res.data;
      }
    });
  },
    resizeChart() {
      if (this.chartInstance) this.chartInstance.resize();
    },
    addHistory() {
        request.post("/history/add", {
           userId: this.user.id,
           bookId: this.id
        });
     },
    checkCollection() {
      if (!this.user.id) return;
      request.get("/shelf/check", { params: { userId: this.user.id, bookId: this.id } }).then(res => {
        if (res.code == 200) this.isCollected = res.data;
      })
    },
    getMatchColor(score) {
      if (score >= 90) return '#ff7675'; 
      if (score >= 80) return '#fab1a0'; 
      return '#74b9ff'; 
    },
    loadRelated() {
      request.get("/recommend/related/" + this.id).then(res => {
        if(res.code == 200) this.relatedBooks = res.data;
      })
    },
    goRelated(id) {
      this.$router.push("/book/" + id);
    },
    toggleCollect() {
      if (!this.user.id) {
        this.$message.warning("请登录后收藏");
        return;
      }
      if (this.isCollected) {
        request.delete("/shelf/remove", { params: { userId: this.user.id, bookId: this.id } }).then(res => {
          if (res.code == 200) { this.isCollected = false; this.$message.success("已移出书架"); }
        })
      } else {
        request.post("/shelf/add", { userId: this.user.id, bookId: this.id }).then(res => {
          if (res.code == 200) { this.isCollected = true; this.$message.success("收藏成功"); }
        })
      }
    },
    initWordCloud() {
      if (!this.book.description) return;
      
      // 先销毁旧实例
      if (this.chartInstance) {
        this.chartInstance.dispose();
      }

      request.post("/recommend/keywords", { text: this.book.description }).then(res => {
        if (res.code == 200 && res.data && res.data.length > 0) {
          this.hasKeywords = true;
          this.$nextTick(() => {
            const chartDom = document.getElementById('wordCloud');
            if(!chartDom) return;
            
            this.chartInstance = echarts.init(chartDom);
            this.chartInstance.setOption({
              tooltip: {},
              series: [{
                type: 'wordCloud',
                gridSize: 6,
                sizeRange: [14, 50],
                rotationRange: [12, 60],
                shape: 'circle',
                width: '100%',
                height: '100%',
                drawOutOfBound: false,
                textStyle: {
                  fontFamily: 'sans-serif',
                  fontWeight: 'bold',
                  color: function () {
                    // 使用 App.vue 定义的主色系的变种，或者随机莫兰迪色
                    const colors = ['#5c7c8a', '#e09f7d', '#fab1a0', '#74b9ff', '#a29bfe'];
                    return colors[Math.floor(Math.random() * colors.length)];
                  }
                },
                data: res.data
              }]
            });
            window.addEventListener('resize', this.resizeChart);
          });
        } else {
          this.hasKeywords = false;
        }
      })
    },
    loadDetail() {
      request.get("/book/" + this.id).then(res => {
        if(res.code == 200) {
          this.book = res.data;
          this.$nextTick(() => { this.initWordCloud(); });
        }
      })
    },
    loadComments() {
      // 假设后端返回的数据里包含 user info (nickname, avatarUrl)
      request.get("/rating/list?bookId=" + this.id).then(res => {
        if(res.code == 200) this.comments = res.data;
      })
    },
    submitRating() {
      if (this.form.score === 0) { this.$message.warning("请先打分"); return; }
      request.post("/rating/add", {
        userId: this.user.id, bookId: this.book.id, score: this.form.score, comment: this.form.comment
      }).then(res => {
        if (res.code == 200) {
          this.$message.success("评价成功");
          this.form = { score: 0, comment: '' };
          this.loadDetail(); // 刷新评分
          this.loadComments(); // 刷新评论列表
        }
      })
    }
  }
}
</script>

<style scoped>
/* ================= 1. 全局布局与背景 ================= */
.page-container {
  min-height: 100vh;
  position: relative;
  /* 建议在父级处理 padding-top 以适配导航栏 */
}

/* 背景虚化层 */
.ambient-bg {
  position: absolute;
  top: -60px;
  left: 0;
  right: 0;
  height: 60vh;
  background-size: cover;
  background-position: center;
  filter: blur(40px) saturate(0.8);
  opacity: 0.5;
  z-index: 0;
  mask-image: linear-gradient(to bottom, black 0%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, black 0%, transparent 100%);
}

.main-content {
  position: relative;
  z-index: 10;
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* ================= 2. 左侧 Sticky 侧边栏 ================= */
.sidebar-wrapper {
  position: relative;
}

.sticky-sidebar {
  position: sticky;
  top: 100px;
  text-align: center;
}

.cover-box {
  position: relative;
  width: 100%;
  margin-bottom: 24px;
  perspective: 1000px;
}

.book-cover {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  transition: transform 0.3s ease;
}

.book-cover:hover {
  transform: translateY(-5px) rotateY(5deg);
}

/* 评分概览 */
.rating-overview {
  margin-top: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-md);
  text-align: center;
}

.rating-overview .score {
  font-size: 3rem;
  font-weight: 800;
  color: var(--text-main);
  line-height: 1;
  margin-bottom: 5px;
}

.rating-overview .count {
  font-size: 0.85rem;
  color: var(--text-light);
  margin-top: 8px;
}

/* ================= 3. 按钮组布局 (已优化冲突) ================= */
.action-buttons {
  margin-top: 20px;
  display: flex;
  flex-direction: column; /* 垂直排列 */
  gap: 12px; /* 按钮间距 */
}

/* 立即阅读按钮 - 采用最下方的稳重蓝风格 */
.action-buttons .btn-read {
  width: 100%;
  margin: 0 !important; 
  padding: 12px;
  font-weight: 600;
  font-size: 1rem;
  background-color: #409EFF; 
  border: 1px solid #409EFF;
  color: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
}

.action-buttons .btn-read:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.3);
}

/* 加入书架按钮 - 保持样式统一 */
.action-buttons .btn-collect {
  width: 100%;
  margin: 0 !important; 
  padding: 12px;
  font-weight: 600;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.action-buttons .btn-collect:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

/* ================= 4. 右侧内容面板 ================= */
.glass-panel-lg {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-md);
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 2rem;
  margin: 0 0 16px 0;
  color: var(--text-main);
  line-height: 1.3;
}

.book-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 1rem;
}

.meta-divider { opacity: 0.3; }

.category-tag { 
  border: none; 
  background: rgba(92, 124, 138, 0.1); 
  color: var(--color-primary); 
}

/* 通用区块样式 */
.section-block {
  margin-top: 32px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-main);
}

.description-text {
  font-size: 1rem;
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-wrap;
  text-align: justify;
}

.soft-divider {
  margin: 40px 0;
  background-color: rgba(0,0,0,0.05);
}

/* AI词云区块 */
.word-cloud-container {
  background: rgba(0,0,0,0.02); 
  border-radius: 12px;
  padding: 10px;
  margin-top: 10px;
  border: 1px solid rgba(0,0,0,0.03);
}

.word-cloud-box {
  width: 100%;
  height: 160px; /* 采用下方精简高度 */
}

/* ================= 5. 关联推荐 Grid ================= */
.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
}

.mini-book-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.mini-book-card:hover { 
  transform: translateY(-5px); 
}

.mini-cover {
  position: relative;
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-bottom: 8px;
  box-shadow: var(--shadow-sm);
  aspect-ratio: 2 / 3;
}

.mini-cover img { width: 100%; height: 100%; object-fit: cover; }

.match-badge {
  position: absolute; bottom: 0; right: 0;
  color: white; font-size: 10px; padding: 2px 6px;
  border-top-left-radius: 6px;
  font-weight: bold;
}

.mini-title {
  font-size: 0.85rem;
  color: var(--text-secondary);
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis;
  text-align: center;
}

/* ================= 6. 评论区布局 ================= */
.user-comment-box {
  background: #f8f9fb;
  padding: 20px;
  border-radius: var(--radius-md);
}

.rate-row { margin-bottom: 10px; display: flex; align-items: center; color: var(--text-secondary); }
.action-row { margin-top: 10px; text-align: right; }
.login-placeholder { text-align: center; padding: 20px; background: #f8f9fb; border-radius: var(--radius-md); }

/* 评论滚动容器 - 统一使用下方的 wrapper 逻辑 */
.comments-scroll-wrapper {
  max-height: 500px; 
  overflow-y: auto;
  margin-top: 20px;
  padding-right: 10px;
}

/* 滚动条美化 */
.comments-scroll-wrapper::-webkit-scrollbar {
  width: 5px;
}
.comments-scroll-wrapper::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 10px;
}
.comments-scroll-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

/* 评论单项 */
.comment-item { 
  display: flex; 
  gap: 16px; 
  padding: 16px 0; 
  border-bottom: 1px solid rgba(0,0,0,0.03); 
}

.comment-body { flex: 1; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.comment-author { font-weight: 600; color: var(--text-main); font-size: 0.95rem; }
.comment-date { margin-left: auto; font-size: 0.8rem; color: #ccc; }
.comment-text { color: var(--text-secondary); line-height: 1.6; margin: 0; }

.no-more-msg, .comments-end-tag {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 20px 0;
}
</style>