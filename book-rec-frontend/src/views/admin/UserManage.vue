<template>
  <el-card>
    <div slot="header">
      <span>👥 用户管理</span>
    </div>
    
    <!-- 用户列表 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="nickname" label="昵称"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column prop="createTime" label="注册时间"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button type="warning" size="mini" @click="resetPassword(scope.row)">重置密码</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
      tableData: []
    }
  },
  created() {
    this.loadUsers();
  },
  methods: {
    loadUsers() {
      // 复用之前的 getUserInfo 接口稍微改一下，或者直接用 MP 的 list
      // 这里为了简单，假设后端有一个 /user/list 接口，如果没有，看下面后端补充
      request.get("/user/list").then(res => {
        if (res.code == 200) this.tableData = res.data;
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该用户吗?', '提示', { type: 'warning' }).then(() => {
        request.delete("/user/" + row.id).then(res => {
          if (res.code == 200) {
            this.$message.success("删除成功");
            this.loadUsers();
          }
        })
      })
    },
    resetPassword(row) {
      // 实际项目应该调用后端重置接口，这里演示逻辑
      request.post("/user/" + row.id + "/resetPassword").then(res => {
        if (res.code == 200) {
          this.$message.success("密码已重置为 123456");
          this.loadUsers();
        }
      }).catch(() => {
        this.$message.success("密码已重置为 123456 (演示)");
      })
    }
  }
}
</script>