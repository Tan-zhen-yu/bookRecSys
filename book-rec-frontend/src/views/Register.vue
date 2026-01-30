<template>
  <div class="login-container">
    <div class="bg-decoration-1"></div>
    <div class="bg-decoration-2"></div>

    <div class="login-box-wrapper">
      <el-card class="login-card" :body-style="{ padding: '40px' }">
        <div class="login-header">
          <div class="logo-area">
            <i class="el-icon-edit-outline"></i>
          </div>
          <h1 class="login-title">加入我们</h1>
          <p class="login-subtitle">开启您的智慧阅读之旅</p>
        </div>

        <el-form :model="form" :rules="rules" ref="registerForm" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              prefix-icon="el-icon-user"
              placeholder="请设置用户名"
              class="custom-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              prefix-icon="el-icon-lock"
              placeholder="请设置密码"
              show-password
              class="custom-input"
            ></el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword" style="margin-bottom: 30px;">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              prefix-icon="el-icon-circle-check"
              placeholder="请再次确认密码"
              show-password
              class="custom-input"
              @keyup.enter.native="handleRegister"
            ></el-input>
          </el-form-item>

          <div class="action-area">
            <el-button type="primary" class="login-btn" @click="handleRegister" :loading="loading">
              立即注册
            </el-button>
            <div class="footer-links">
              <span>已有账号?</span>
              <el-button type="text" class="reg-link" @click="$router.push('/login')">返回登录</el-button>
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
  name: 'Register',
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: ''
      },
      loading: false,
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请设置密码', trigger: 'blur' }, { min: 6, message: '长度至少6位', trigger: 'blur' }],
        confirmPassword: [{ validator: validatePass, trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          // 提交给后端的通常不需要 confirmPassword 字段
          const { username, password } = this.form;
          request.post("/user/register", { username, password }).then(res => {
            this.loading = false;
            if (res.code == 200) {
              this.$message.success("注册成功，请登录");
              this.$router.push("/login");
            } else {
              this.$message.error(res.msg || "注册失败");
            }
          }).catch(() => { this.loading = false; });
        }
      });
    }
  }
}
</script>

<style scoped>
@import "@/assets/styles/auth.css"; /* 使用 @ 符号通常更稳妥，指向 src 目录 */
</style>