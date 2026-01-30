<template>
  <div class="square-container">
    
    <!-- 1. 头部引导 -->
    <div class="header-section">
      <h1 class="page-title">📖 书友回响</h1>
      <p class="subtitle">在这里，听见思想碰撞的声音</p>
    </div>

    <!-- 2. 评论卡片网格 -->
    <div class="reviews-wrapper">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" v-for="(item, index) in activities" :key="item.id" class="grid-col">
          
          <div class="review-card glass-panel" :style="{ animationDelay: index * 0.1 + 's' }">
            <!-- 装饰引号 -->
            <div class="quote-decoration">❝</div>

            <!-- 用户信息行 -->
            <div class="user-row">
              <el-avatar 
                :size="40" 
                :src="item.avatarUrl" 
                class="user-avatar"
                style="background: var(--color-primary)">
                {{ item.nickname ? item.nickname.charAt(0) : 'U' }}
              </el-avatar>
              <div class="user-info">
                <div class="username">{{ item.nickname || '匿名书友' }}</div>
                <div class="post-time">{{ formatDate(item.createTime) }}</div>
              </div>
            </div>

            <!-- 评论内容 -->
            <div class="comment-content">
              {{ item.comment }}
            </div>

            <el-divider class="card-divider"></el-divider>

            <!-- 关联书籍 (点击跳转) -->
            <div class="book-link-box" @click="$router.push('/book/' + item.bookId)">
              <img :src="item.bookCover || 'https://via.placeholder.com/60x80'" class="book-cover-mini">
              <div class="book-meta">
                <div class="book-title">《{{ item.bookTitle }}》</div>
                <el-rate 
                  :value="Number(item.score)" 
                  disabled 
                  text-color="#ff9900" 
                  class="mini-rate">
                </el-rate>
              </div>
              <i class="el-icon-arrow-right arrow-icon"></i>
            </div>

          </div>

        </el-col>
      </el-row>
      
      <el-empty v-if="activities.length === 0" description="广场上静悄悄的，快去发第一条书评吧！"></el-empty>
    </div>

  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Square",
  data() {
    return {
      activities: []
    }
  },
  created() {
    this.loadSquare();
  },
  methods: {
    loadSquare() {
      request.get("/rating/square").then(res => {
        if(res.code == 200) {
          this.activities = res.data;
        }
      })
    },
    // 简单的相对时间转换 (可选，如果后端返回的是绝对时间字符串)
    formatDate(dateStr) {
      if(!dateStr) return '';
      // 如果需要更复杂的 "3小时前" 逻辑，可以用 dayjs
      return dateStr.split(' ')[0]; // 只显示日期，保持简洁
    }
  }
}
</script>

<style scoped>
.square-container {
  padding-bottom: 60px;
}

.header-section {
  text-align: center;
  margin-bottom: 50px;
  animation: fadeDown 0.8s ease;
}

.page-title {
  font-size: 2rem;
  color: var(--text-main);
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
}

/* 网格布局调整 */
.grid-col {
  margin-bottom: 24px;
}

/* 卡片主体 */
.review-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  height: 100%;
  position: relative;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid rgba(0,0,0,0.02);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  animation: fadeUp 0.6s ease-out backwards;
  overflow: hidden;
}

.review-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
  border-color: rgba(92, 124, 138, 0.2);
}

/* 巨大的引号装饰 */
.quote-decoration {
  position: absolute;
  top: -10px;
  right: 20px;
  font-size: 80px;
  font-family: Georgia, serif;
  color: var(--color-primary);
  opacity: 0.08;
  pointer-events: none;
  line-height: 1;
}

/* 用户信息 */
.user-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.user-avatar {
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-info {
  margin-left: 12px;
}

.username {
  font-weight: 700;
  color: var(--text-main);
  font-size: 0.95rem;
}

.post-time {
  font-size: 0.75rem;
  color: #ccc;
  margin-top: 2px;
}

/* 评论正文 */
.comment-content {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 0.95rem;
  flex: 1; /* 让内容区撑开高度 */
  min-height: 60px;
  /* 限制最大行数，太长显示省略号，保持卡片整齐 */
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 5; 
  overflow: hidden;
  margin-bottom: 10px;
  font-family: 'PingFang SC', sans-serif;
}

.card-divider {
  margin: 16px 0;
  background-color: rgba(0,0,0,0.04);
}

/* 底部书籍链接 */
.book-link-box {
  display: flex;
  align-items: center;
  background: #f8f9fb;
  padding: 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.3s;
}

.book-link-box:hover {
  background: #f0f2f5;
}

.book-cover-mini {
  width: 36px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.book-meta {
  flex: 1;
  overflow: hidden;
}

.book-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  margin-bottom: 2px;
}

.mini-rate {
  transform: scale(0.9);
  transform-origin: left;
}

.arrow-icon {
  color: #ccc;
  font-size: 14px;
}

/* 动画定义 */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>