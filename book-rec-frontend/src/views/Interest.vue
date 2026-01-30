<template>
  <div class="interest-container">
    <div class="content-box">
      <!-- 标题区 -->
      <div style="text-align: center; margin-bottom: 40px;">
        <h1 style="font-size: 28px; color: #333;">📚 选择你感兴趣的领域</h1>
        <p style="color: #999; font-size: 16px; margin-top: 10px;">
          我们将根据你的选择，为你定制个性化推荐内容
        </p>
      </div>

      <!-- 标签选择区 -->
      <div class="tag-cloud">
        <div 
          v-for="cat in categories" 
          :key="cat.id" 
          class="tag-item" 
          :class="{ active: selectedTags.includes(cat.id) }"
          @click="toggleTag(cat.id)">
          
          <!-- 这里的图标可以是静态的，或者根据分类名判断 -->
          <i class="el-icon-collection-tag" style="margin-right: 5px;"></i>
          {{ cat.name }}
          
          <!-- 选中时的对勾图标 -->
          <div class="check-mark" v-if="selectedTags.includes(cat.id)">
            <i class="el-icon-check"></i>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div style="text-align: center; margin-top: 50px;">
        <el-button 
          type="primary" 
          round 
          style="width: 200px; height: 50px; font-size: 18px; font-weight: bold; box-shadow: 0 10px 20px rgba(64, 158, 255, 0.3);"
          :disabled="selectedTags.length === 0"
          @click="saveInterest">
          开启阅读之旅
        </el-button>
        <div style="margin-top: 20px;">
          <el-button type="text" style="color: #bbb" @click="skip">跳过，先随便看看</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
      // 1. 先尝试从本地拿 ID，如果没有 ID 说明没登录，踢回登录页
      user: JSON.parse(localStorage.getItem("user") || '{}'),
      categories: [],
      selectedTags: [] 
    }
  },
  created() {
    if (!this.user.id) {
      this.$router.push('/login');
      return;
    }
    
    this.loadCategories();
    // 2. 关键修改：不要直接用本地缓存的 Tags，而是去后端查最新的
    this.fetchLatestUser();
  },
  methods: {
    // 获取分类列表
    loadCategories() {
      request.get("/category/list").then(res => {
        if(res.code == 200) this.categories = res.data;
      })
    },

    // --- 新增：获取最新用户信息并初始化标签 ---
    fetchLatestUser() {
      request.get("/user/" + this.user.id).then(res => {
        if (res.code == 200) {
          // 更新内存中的用户信息
          this.user = res.data; 
          
          // 更新本地缓存 (保持同步)
          localStorage.setItem("user", JSON.stringify(this.user));

          // 初始化选中的标签
          if (this.user.tags) {
            // "1,2,3" -> [1, 2, 3]
            // filter(Boolean) 是为了防止出现 "1,,2" 这种空字符串导致转成 0
            this.selectedTags = this.user.tags.split(',')
                                  .filter(Boolean)
                                  .map(Number);
          } else {
            this.selectedTags = [];
          }
        }
      })
    },

    // 切换选中/取消 (逻辑不变，这是标准的 Toggle 逻辑)
    toggleTag(id) {
      const index = this.selectedTags.indexOf(id);
      if (index > -1) {
        // 如果已经有了，就删除 (取消选中)
        this.selectedTags.splice(index, 1); 
      } else {
        // 如果没有，就添加 (选中)
        this.selectedTags.push(id); 
      }
    },

    // 保存并跳转
    saveInterest() {
      // 将数组转回字符串 "1,2,3"
      const tagsStr = this.selectedTags.join(',');
      
      request.post("/user/tags", {
        userId: this.user.id,
        tags: tagsStr
      }).then(res => {
        if (res.code == 200) {
          this.$message.success("设置成功！");
          
          // 更新本地缓存 user 对象里的 tags
          this.user.tags = tagsStr;
          localStorage.setItem("user", JSON.stringify(this.user));
          
          // 跳转首页
          this.$router.push("/");
        } else {
          this.$message.error(res.msg);
        }
      })
    },

    skip() {
      this.$router.push("/");
    }
  }
}
</script>

<style scoped>
.interest-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.content-box {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  padding: 60px;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
  width: 800px;
  max-width: 100%;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.tag-item {
  padding: 15px 30px;
  background: #f0f2f5;
  border-radius: 50px;
  font-size: 16px;
  color: #555;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  border: 2px solid transparent;
  user-select: none;
}

.tag-item:hover {
  transform: translateY(-3px);
  background: #fff;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
}

.tag-item.active {
  background: #eef7ff;
  color: #409EFF;
  border-color: #409EFF;
  font-weight: bold;
  padding-right: 40px; /* 给对勾留位置 */
}

.check-mark {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #409EFF;
}
</style>