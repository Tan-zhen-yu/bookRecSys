<template>
  <el-card>
    <div slot="header">
      <span>📚 图书管理</span>
      <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增图书</el-button>
    </div>

    <!-- 搜索与筛选 -->
    <div style="margin-bottom: 20px;">
      <el-input v-model="search" placeholder="输入书名搜索" style="width: 200px; margin-right: 10px"></el-input>
      <el-button type="primary" @click="loadBooks">查询</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column label="封面" width="100">
        <template slot-scope="scope">
          <img :src="scope.row.coverUrl" style="width: 50px; height: 70px">
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名"></el-table-column>
      <el-table-column prop="author" label="作者"></el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100">
         <!-- 这里如果后端没返categoryName，暂时不显示 -->
      </el-table-column>
      <el-table-column prop="price" label="价格"></el-table-column>
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      style="margin-top: 20px"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      @current-change="handlePageChange">
    </el-pagination>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="form.id ? '编辑图书' : '新增图书'" :visible.sync="dialogVisible">
      <el-form :model="form" label-width="80px">
        <el-form-item label="书名">
          <el-input v-model="form.title"></el-input>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author"></el-input>
        </el-form-item>
        <el-form-item label="分类ID">
           <!-- 简化处理，直接填ID，优化可以做下拉框 -->
          <el-input v-model="form.categoryId" placeholder="1-科幻, 2-编程..."></el-input>
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="form.coverUrl"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveBook">确 定</el-button>
      </span>
    </el-dialog>

  </el-card>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      search: '',
      dialogVisible: false,
      form: {}
    }
  },
  created() {
    this.loadBooks();
  },
  methods: {
    loadBooks() {
      request.get("/book/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.search
        }
      }).then(res => {
        if(res.code == 200) {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      })
    },
    handlePageChange(page) {
      this.pageNum = page;
      this.loadBooks();
    },
    handleAdd() {
      this.form = {};
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row)); // 深拷贝
      this.dialogVisible = true;
    },
    handleDelete(row) {
      this.$confirm('确认删除?', '提示').then(() => {
        request.delete("/book/" + row.id).then(res => {
          if(res.code == 200) {
            this.$message.success("删除成功");
            this.loadBooks();
          }
        })
      })
    },
    saveBook() {
      request.post("/book/save", this.form).then(res => {
        if(res.code == 200) {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.loadBooks();
        }
      })
    }
  }
}
</script>