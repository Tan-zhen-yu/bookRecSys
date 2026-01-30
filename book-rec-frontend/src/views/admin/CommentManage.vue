<template>
  <el-card>
    <div slot="header">
      <span>💬 评论审核</span>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
      <el-table-column prop="bookId" label="图书ID" width="100"></el-table-column>
      <el-table-column prop="score" label="评分" width="80">
         <template slot-scope="scope">
           <el-tag type="warning">{{ scope.row.score }}</el-tag>
         </template>
      </el-table-column>
      <el-table-column prop="comment" label="评论内容"></el-table-column>
      <el-table-column prop="createTime" label="时间" width="160"></el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">违规删除</el-button>
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
    this.loadComments();
  },
  methods: {
    loadComments() {
      request.get("/rating/listAll").then(res => {
        if(res.code == 200) this.tableData = res.data;
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除这条评论吗?', '提示', { type: 'warning' }).then(() => {
        request.delete("/rating/" + row.id).then(res => {
           if(res.code == 200) {
             this.$message.success("已删除");
             this.loadComments();
           }
        })
      })
    }
  }
}
</script>