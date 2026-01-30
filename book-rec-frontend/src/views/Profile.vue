<template>
  <div class="profile-container">
    <el-row :gutter="30">
      
      <!-- ================= 左侧：个人档案 & 数据分析 ================= -->
      <el-col :xs="24" :sm="8" :md="7" :lg="6">
        
        <!-- 1. 用户卡片 -->
        <div class="side-card profile-card">
          <div class="card-bg"></div>
          
          <!-- 编辑按钮 (绝对定位在右上角) -->
          <div class="settings-btn" @click="openSettings">
            <i class="el-icon-setting"></i>
          </div>

          <div class="profile-content">
            <el-avatar 
              :size="90" 
              :src="user.avatarUrl" 
              class="user-avatar"
              style="font-size: 32px; background: var(--color-primary);">
              {{ user.nickname ? user.nickname.charAt(0) : 'U' }}
            </el-avatar>
            <h2 class="nickname">{{ user.nickname }}</h2>
            <div class="username">ID: {{ user.username }}</div>
            
            <div class="stats-row">
              <div class="stat-item">
                <span class="num">{{ shelfBooks.length }}</span>
                <span class="label">藏书</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="num">{{ myComments.length }}</span>
                <span class="label">书评</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 兴趣画像卡片 (包含雷达图 + 标签) -->
        <div class="side-card chart-card">
          <div class="card-header">
            <span><i class="el-icon-data-analysis"></i> 阅读DNA</span>
            <el-button type="text" size="small" @click="$router.push('/interest')">重选标签</el-button>
          </div>
          
          <!-- 雷达图 -->
          <div id="radarChart" class="radar-box"></div>
          <div v-if="!hasRadarData" class="chart-empty">暂无阅读数据</div>
          
          <!-- 兴趣标签展示区 -->
          <div class="tags-section">
            <div class="tags-title">我的兴趣标签</div>
            <div v-if="userTagsList.length > 0" class="tags-wrapper">
              <span v-for="(tag, index) in userTagsList" :key="index" class="interest-tag">
                {{ getTagName(tag) }}
              </span>
            </div>
            <div v-else class="no-tags">未设置兴趣标签</div>
          </div>
        </div>

      </el-col>

      <!-- ================= 右侧：书架/历史/评论 Tab (保持不变) ================= -->
      <el-col :xs="24" :sm="16" :md="17" :lg="18">
        <div class="main-panel glass-panel">
          <el-tabs v-model="activeTab" class="custom-tabs" @tab-click="handleTabClick">
            
            <!-- Tab 1: 书架 -->
            <el-tab-pane name="shelf">
              <span slot="label"><i class="el-icon-collection"></i> 我的书架</span>
              <div v-if="shelfBooks.length > 0" class="books-grid">
                <div v-for="book in shelfBooks" :key="book.id" class="shelf-item" @click="$router.push('/book/' + book.id)">
                  <div class="cover-wrapper">
                    <img :src="book.coverUrl" class="book-cover">
                    <div class="hover-mask">
                      <el-button type="danger" icon="el-icon-delete" circle size="small" @click.stop="removeFromShelf(book.id)"></el-button>
                    </div>
                  </div>
                  <div class="book-info">
                    <div class="book-title">{{ book.title }}</div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="书架空空如也"></el-empty>
            </el-tab-pane>

            <!-- Tab 2: 历史 -->
            <el-tab-pane name="history">
              <span slot="label"><i class="el-icon-time"></i> 浏览足迹</span>
              <div v-if="historyBooks.length > 0" class="history-list">
                 <el-timeline>
                    <el-timeline-item v-for="(book, index) in historyBooks" :key="index" hide-timestamp color="var(--color-primary)">
                       <div class="history-item glass-hover" @click="$router.push('/book/' + book.id)">
                          <img :src="book.coverUrl" class="history-cover">
                          <div class="history-info">
                             <div class="h-title">{{ book.title }}</div>
                             <div class="h-author">{{ book.author }}</div>
                             <div class="h-time">最近访问</div>
                          </div>
                       </div>
                    </el-timeline-item>
                 </el-timeline>
              </div>
              <el-empty v-else description="暂无历史"></el-empty>
            </el-tab-pane>

            <!-- Tab 3: 评论 -->
            <el-tab-pane name="comment">
               <span slot="label"><i class="el-icon-chat-line-square"></i> 我的书评</span>
               <div v-if="myComments.length > 0" class="comments-stream">
                 <div v-for="item in myComments" :key="item.id" class="my-comment-card">
                    <div class="comment-header">
                        <span class="book-link" @click="$router.push('/book/' + item.bookId)">《{{ item.bookTitle }}》</span>
                        <el-rate :value="Number(item.score)" disabled class="mini-rate"></el-rate>
                        <span class="comment-date">{{ item.createTime }}</span>
                         <el-button 
                          type="text" 
                          icon="el-icon-delete" 
                          class="delete-comment-btn"
                          @click="deleteComment(item.id)">
                          删除
                      </el-button>
                    </div>
                    <div class="comment-bubble">{{ item.comment }}</div>
                 </div>
               </div>
               <el-empty v-else description="暂无评论"></el-empty>
            </el-tab-pane>

          </el-tabs>
        </div>
      </el-col>
    </el-row>

    <!-- ================= 抽屉：修改资料 ================= -->
    <el-drawer
      title="账号设置"
      :visible.sync="drawerVisible"
      direction="rtl"
      size="350px">
      
      <div style="padding: 0 20px;">
        <el-tabs v-model="settingTab">
          <!-- Tab A: 基本资料 -->
          <el-tab-pane label="基本资料" name="basic">
            <el-form :model="editForm" label-position="top">
              <el-form-item label="昵称">
                <el-input v-model="editForm.nickname"></el-input>
              </el-form-item>
              <el-form-item label="头像URL">
                <el-input v-model="editForm.avatarUrl" placeholder="输入图片链接"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" style="width: 100%" @click="saveInfo">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- Tab B: 安全设置 -->
          <el-tab-pane label="安全设置" name="security">
            <el-form :model="passForm" label-position="top">
              <el-form-item label="原密码">
                <el-input v-model="passForm.oldPass" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passForm.newPass" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="danger" style="width: 100%" @click="savePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

  </div>
</template>

<script>
import request from "@/utils/request";
import * as echarts from 'echarts';

export default {
  name: "Profile",
  data() {
    return {
      user: JSON.parse(localStorage.getItem("user") || '{}'),
      categories: [], // 所有的分类字典
      activeTab: 'shelf',
      shelfBooks: [],
      historyBooks: [],
      myComments: [],
      chartInstance: null,
      hasRadarData: false,
      
      // 抽屉相关
      drawerVisible: false,
      settingTab: 'basic',
      editForm: {},
      passForm: { oldPass: '', newPass: '' }
    }
  },
  computed: {
    // 把 "1,3" 转为数组 [1, 3]
    userTagsList() {
      if (!this.user.tags) return [];
      return this.user.tags.split(',');
    }
  },
  mounted() {
    this.loadAllData();
    window.addEventListener('resize', this.resizeChart);
  },
  beforeDestroy() {
    if (this.chartInstance) this.chartInstance.dispose();
    window.removeEventListener('resize', this.resizeChart);
  },
  methods: {
    loadAllData() {
      this.loadShelf();
      this.loadHistory();
      this.loadComments();
      this.loadCategories(); // 加载分类字典用于显示标签名
      this.$nextTick(() => { this.initRadar(); });
    },
      deleteComment(id) {
      this.$confirm('确定删除这条评论吗?', '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用之前写的删除接口 (注意：这里复用了管理员的删除接口，如果是普通用户建议后端加个权限校验，但在毕设里通常通用)
        request.delete("/rating/" + id).then(res => {
          if (res.code == 200) {
            this.$message.success("删除成功");
            this.loadComments(); // 刷新列表
            
            // 如果删除了评论，可能会影响雷达图数据，建议也刷新一下雷达图
            this.initRadar();
          } else {
            this.$message.error(res.msg);
          }
        })
      }).catch(() => {});
    },
    
    // 打开设置抽屉
    openSettings() {
      this.editForm = { ...this.user }; // 复制用户信息
      this.passForm = { oldPass: '', newPass: '' };
      this.drawerVisible = true;
    },

    // 保存基本信息
    saveInfo() {
      request.post("/user/update", this.editForm).then(res => {
        if (res.code == 200) {
          this.$message.success("保存成功");
          
          // 1. 更新本地数据
          this.user = { ...this.user, ...this.editForm }; 
          localStorage.setItem("user", JSON.stringify(this.user));
          
          // 2. 【新增】关键一步：通知 Layout 组件更新用户信息
          this.$root.$emit('updateUser', this.user);
          
          this.drawerVisible = false;
        }
      })
    },

    // 修改密码
    savePassword() {
      if (!this.passForm.oldPass || !this.passForm.newPass) {
        this.$message.warning("请填写完整");
        return;
      }
      request.post("/user/password", {
        userId: this.user.id,
        oldPass: this.passForm.oldPass,
        newPass: this.passForm.newPass
      }).then(res => {
        if (res.code == 200) {
          this.$message.success("密码修改成功，请重新登录");
          localStorage.removeItem("user");
          this.$router.push("/login");
        } else {
          this.$message.error(res.msg);
        }
      })
    },

    // 获取分类名称 (Helper)
    getTagName(id) {
      const cat = this.categories.find(c => c.id == id);
      return cat ? cat.name : '未知';
    },

    loadCategories() {
      request.get("/category/list").then(res => {
        if(res.code == 200) this.categories = res.data;
      })
    },
    
    // ... 原有的 initRadar, loadShelf, loadHistory, loadComments, removeFromShelf 保持不变 ...
    resizeChart() { if (this.chartInstance) this.chartInstance.resize(); },
    initRadar() {
      request.get("/user/radar/" + this.user.id).then(res => {
        if (res.code == 200 && res.data.indicators && res.data.indicators.length > 0) {
          this.hasRadarData = true;
          const chartDom = document.getElementById('radarChart');
          if (!chartDom) return;
          this.chartInstance = echarts.init(chartDom);
          this.chartInstance.setOption({
            radar: { indicator: res.data.indicators, radius: '65%' },
            series: [{
              type: 'radar',
              areaStyle: { color: 'rgba(92, 124, 138, 0.4)' },
              data: [{ value: res.data.values, name: '阅读偏好' }]
            }]
          });
        }
      })
    },
    loadShelf() {
      request.get("/shelf/my", { params: { userId: this.user.id } }).then(res => {
        if (res.code == 200) this.shelfBooks = res.data;
      })
    },
    loadHistory() {
      request.get("/history/my", { params: { userId: this.user.id } }).then(res => {
        if (res.code == 200) this.historyBooks = res.data;
      })
    },
    loadComments() {
      request.get("/rating/my", { params: { userId: this.user.id } }).then(res => {
        if (res.code == 200) this.myComments = res.data;
      })
    },
    removeFromShelf(bookId) {
      request.delete("/shelf/remove", { params: { userId: this.user.id, bookId } }).then(res => {
        if(res.code == 200) { this.loadShelf(); }
      })
    }
  }
}
</script>

<style scoped>
/* ================= 通用样式 ================= */
.profile-container { padding-bottom: 40px; }
.side-card {
  background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden; margin-bottom: 24px; position: relative;
}
.main-panel {
  background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  min-height: 600px; padding: 20px 30px;
}

/* ================= 个人卡片 ================= */
.card-bg { height: 100px; background: linear-gradient(135deg, #74b9ff, #a29bfe); }
.settings-btn {
  position: absolute; top: 15px; right: 15px; color: white; cursor: pointer;
  font-size: 20px; z-index: 2; transition: transform 0.3s;
}
.settings-btn:hover { transform: rotate(90deg); }
.profile-content { position: relative; padding: 0 20px 24px; margin-top: -45px; text-align: center; }
.user-avatar { border: 4px solid white; box-shadow: 0 4px 12px rgba(0,0,0,0.1); margin-bottom: 10px; }
.nickname { margin: 0; color: #333; font-size: 1.4rem; }
.username { color: #999; font-size: 0.9rem; margin-bottom: 20px; }
.stats-row { display: flex; justify-content: center; padding-top: 15px; border-top: 1px solid #eee; }
.stat-item { padding: 0 20px; display: flex; flex-direction: column; }
.stat-item .num { font-weight: bold; font-size: 1.2rem; }
.stat-item .label { font-size: 0.8rem; color: #999; }
.stat-divider { width: 1px; background: #eee; }

/* ================= 兴趣卡片 ================= */
.chart-card { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; font-weight: bold; }
.radar-box { width: 100%; height: 260px; }
.chart-empty { height: 200px; display: flex; align-items: center; justify-content: center; color: #ccc; }

/* 标签区 */
.tags-section { margin-top: 10px; border-top: 1px dashed #eee; padding-top: 15px; }
.tags-title { font-size: 12px; color: #999; margin-bottom: 10px; }
.tags-wrapper { display: flex; flex-wrap: wrap; gap: 8px; }
.interest-tag {
  background: #f0f5ff; color: #409EFF; padding: 4px 10px; border-radius: 12px; font-size: 12px;
}
.no-tags { font-size: 12px; color: #ccc; text-align: center; }

/* ================= 书架列表 (复用 Grid) ================= */
.books-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 15px; padding-top: 15px; }
.shelf-item { cursor: pointer; }
.cover-wrapper { position: relative; border-radius: 6px; overflow: hidden; aspect-ratio: 2/3; margin-bottom: 6px; }
.book-cover { width: 100%; height: 100%; object-fit: cover; }
.hover-mask {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s;
}
.shelf-item:hover .hover-mask { opacity: 1; }
.book-title { font-size: 13px; text-align: center; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }

/* ================= 历史 & 评论 (简化) ================= */
.history-item { display: flex; align-items: center; padding: 10px; background: #f9f9f9; border-radius: 8px; cursor: pointer; margin-bottom: 5px; }
.history-cover { width: 40px; height: 56px; border-radius: 4px; margin-right: 15px; object-fit: cover; }
.h-title { font-weight: bold; font-size: 14px; }
.h-author { font-size: 12px; color: #999; }

.my-comment-card { margin-bottom: 20px; }
.comment-header { 
  display: flex; 
  align-items: center; 
  font-size: 14px; 
  margin-bottom: 5px; 
  position: relative; /* 相对定位 */
}
.book-link { color: #409EFF; cursor: pointer; font-weight: bold; }
.comment-bubble { background: #f4f6f8; padding: 12px; border-radius: 8px; color: #555; font-size: 13px; }
.comment-date { 
  margin-left: auto; 
  font-size: 0.8rem; 
  color: #ccc; 
  margin-right: 15px; /* 给按钮留点空隙 */
}

.delete-comment-btn {
  color: #999;
  padding: 0;
  font-size: 13px;
}
.delete-comment-btn:hover {
  color: #F56C6C; /* 红色 */
}
</style>