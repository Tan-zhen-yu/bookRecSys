<template>
  <div>
    <!-- 1. 顶部四个统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" style="color: #409EFF">
          <div slot="header">用户总数</div>
          <div style="font-size: 24px; font-weight: bold;">{{ stats.userCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="color: #67C23A">
          <div slot="header">图书总数</div>
          <div style="font-size: 24px; font-weight: bold;">{{ stats.bookCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="color: #E6A23C">
          <div slot="header">评分互动</div>
          <div style="font-size: 24px; font-weight: bold;">{{ stats.ratingCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="color: #F56C6C">
          <div slot="header">总访问量</div>
          <div style="font-size: 24px; font-weight: bold;">{{ stats.visitCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 图表区域 -->
    <el-row :gutter="20">
      <!-- 左侧：柱状图 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <div slot="header">🔥 热门图书排行 (Top 10)</div>
          <div id="barChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      
      <!-- 右侧：饼图 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <div slot="header">📚 图书分类分布</div>
          <div id="pieChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import request from "@/utils/request";
import * as echarts from 'echarts'; // 引入 ECharts

export default {
  data() {
    return {
      stats: {
        userCount: 0,
        bookCount: 0,
        ratingCount: 0,
        visitCount: 0
      }
    }
  },
  mounted() {
    this.loadStats();
    this.initCharts();
  },
  methods: {
    loadStats() {
      request.get("/admin/stats").then(res => {
        if(res.code == 200) {
          this.stats = res.data;
        }
      })
    },
    initCharts() {
      // 1. 加载并渲染饼图
      request.get("/admin/categoryStats").then(res => {
        if(res.code == 200) {
          this.renderPieChart(res.data);
        }
      });

      // 2. 加载并渲染柱状图
      request.get("/admin/hotBooks").then(res => {
        if(res.code == 200) {
          this.renderBarChart(res.data);
        }
      });
    },
    renderPieChart(data) {
      let chart = echarts.init(document.getElementById('pieChart'));
      let option = {
        tooltip: { trigger: 'item' },
        legend: { top: '5%', left: 'center' },
        series: [
          {
            name: '图书分类',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
            data: data // 填入后端返回的数据
          }
        ]
      };
      chart.setOption(option);
    },
    renderBarChart(data) {
      let chart = echarts.init(document.getElementById('barChart'));
      // 提取书名和热度
      let titles = data.map(item => item.title);
      let counts = data.map(item => item.ratingCount);

      let option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: [
          {
            type: 'category',
            data: titles,
            axisTick: { alignWithLabel: true },
            axisLabel: { interval: 0, rotate: 30 } // 标签旋转防止重叠
          }
        ],
        yAxis: [ { type: 'value' } ],
        series: [
          {
            name: '评分人数',
            type: 'bar',
            barWidth: '60%',
            data: counts,
            itemStyle: { color: '#409EFF' }
          }
        ]
      };
      chart.setOption(option);
    }
  }
}
</script>