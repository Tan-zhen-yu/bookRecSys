<template>
  <div class="login-container">
    <!-- 背景装饰元素，增加呼吸感 -->
    <div class="bg-decoration-1"></div>
    <div class="bg-decoration-2"></div>

    <div class="login-box-wrapper">
      <el-card class="login-card" :body-style="{ padding: '40px' }">
        <!-- 头部标题 -->
        <div class="login-header">
          <div class="logo-area">
            <i class="el-icon-notebook-2"></i>
          </div>
          <h1 class="login-title">图书推荐系统</h1>
          <p class="login-subtitle">探索知识的边界，遇见心仪的好书</p>
        </div>

        <!-- 登录表单 -->
        <el-form :model="form" ref="form" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              prefix-icon="el-icon-user"
              placeholder="请输入用户名"
              class="custom-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item style="margin-bottom: 30px;">
            <el-input 
              v-model="form.password" 
              type="password" 
              prefix-icon="el-icon-lock"
              placeholder="请输入密码"
              show-password
              class="custom-input"
              @keyup.enter.native="login"
            ></el-input>
          </el-form-item>

          <div class="action-area">
            <el-button type="primary" class="login-btn" @click="login" :loading="loading">
              立即登录
            </el-button>
            <div class="footer-links">
              <span>还没有账号?</span>
              <el-button type="text" class="reg-link" @click="register">点击注册</el-button>
            </div>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
      form: { username: '', password: '' },
      loading: false
    }
  },
  methods: {
    login() {
      request.post("/user/login", this.form).then(res => {
        if (res.code == 200) {
          this.$message.success("登录成功");
          
          let user = res.data;
          localStorage.setItem("user", JSON.stringify(user));
          
          // --- 修改开始：判断冷启动 ---
          if (!user.tags || user.tags.trim() === '') {
             // 如果没标签，跳转选择页
             this.$router.push("/interest");
          } else {
             // 如果有标签，正常去首页
             this.$router.push("/");
          }
          // --- 修改结束 ---
          
        } else {
          this.$message.error(res.msg);
        }
      })
    },
    register() {
  this.$router.push("/register"); // 改为路由跳转，而不是在登录页直接调接口
}
  }
}
</script>

<style scoped>
/* ================= 变量定义 ================= */
.login-container {
  --primary-color: #409EFF;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --text-main: #2d3436;
  --text-light: #94a3b8;
  --radius-lg: 20px;
  
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8fafc;
  overflow: hidden;
  position: relative;
}

/* ================= 背景装饰 ================= */
.bg-decoration-1 {
  position: absolute;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(64,158,255,0.1) 0%, rgba(255,255,255,0) 70%);
  top: -100px;
  right: -100px;
  z-index: 0;
}

.bg-decoration-2 {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(118,75,162,0.05) 0%, rgba(255,255,255,0) 70%);
  bottom: -200px;
  left: -200px;
  z-index: 0;
}

/* ================= 登录卡片 ================= */
.login-box-wrapper {
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 20px;
  animation: slideUp 0.8s ease-out;
}

.login-card {
  border: none !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.08) !important;
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-area {
  width: 64px;
  height: 64px;
  background: var(--primary-gradient);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.logo-area i {
  font-size: 32px;
  color: white;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
  letter-spacing: 1px;
}

.login-subtitle {
  color: var(--text-light);
  font-size: 14px;
  margin-top: 10px;
}

/* ================= 表单定制 ================= */
.custom-input >>> .el-input__inner {
  height: 50px;
  line-height: 50px;
  background-color: #f1f5f9;
  border: 1px solid transparent;
  border-radius: 12px;
  padding-left: 45px !important;
  transition: all 0.3s;
}

.custom-input >>> .el-input__inner:focus {
  background-color: white;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}

.custom-input >>> .el-input__prefix {
  left: 15px;
  font-size: 18px;
  display: flex;
  align-items: center;
}

/* ================= 按钮与动作 ================= */
.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px !important;
  background: var(--primary-gradient) !important;
  border: none !important;
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.2);
  transition: all 0.3s !important;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 25px rgba(102, 126, 234, 0.3);
  opacity: 0.9;
}

.login-btn:active {
  transform: translateY(0);
}

.footer-links {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: var(--text-light);
}

.reg-link {
  font-weight: 600;
  margin-left: 5px;
  color: var(--primary-color) !important;
}

/* ================= 动画 ================= */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端适配 */
@media (max-width: 480px) {
  .login-box-wrapper {
    padding: 15px;
  }
  .login-card {
    padding: 20px !important;
  }
}
</style>