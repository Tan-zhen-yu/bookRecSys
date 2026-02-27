<template>
  <div class="monitoring-dashboard">
    
    <!-- 顶部标题栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <h1 class="page-title">
          <i class="el-icon-pie-chart title-icon"></i>
          微服务监控大盘
        </h1>
        <p class="page-subtitle">实时追踪系统性能、流量与健康状态</p>
      </div>
      <div class="header-right">
        <div class="live-indicator">
          <span class="live-dot"></span>
          <span class="live-text">Live Data</span>
        </div>
      </div>
    </header>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <!-- QPS -->
      <el-col :span="6">
        <div class="metric-card qps-card">
          <div class="metric-header">
            <span class="metric-label">当前 QPS</span>
            <div class="metric-icon qps-icon">
              <i class="el-icon-lightning"></i>
            </div>
          </div>
          <div class="metric-value">
            <span class="value-number">{{ traffic.qps?.toFixed(1) || 0 }}</span>
            <span class="value-unit">req/s</span>
          </div>
        </div>
      </el-col>

      <!-- 系统状态 -->
      <el-col :span="6">
        <div class="metric-card health-card">
          <div class="metric-header">
            <span class="metric-label">系统状态</span>
            <div :class="healthIconClass" class="metric-icon">
              <i :class="healthIcon"></i>
            </div>
          </div>
          <div class="metric-value">
            <span :class="healthTextClass" class="status-text">
              {{ health.overallStatus === 'LOADING' ? '加载中...' : health.overallStatus }}
            </span>
          </div>
        </div>
      </el-col>

      <!-- 错误率 -->
      <el-col :span="6">
        <div class="metric-card error-card">
          <div class="metric-header">
            <span class="metric-label">实时错误率</span>
            <div class="metric-icon error-icon">
              <i class="el-icon-warning"></i>
            </div>
          </div>
          <div class="metric-value">
            <span class="value-number error-number">{{ traffic.errorRate?.toFixed(2) || 0 }}</span>
            <span class="value-unit">%</span>
          </div>
        </div>
      </el-col>

      <!-- 响应时间 -->
      <el-col :span="6">
        <div class="metric-card response-card">
          <div class="metric-header">
            <span class="metric-label">平均响应时间</span>
            <div class="metric-icon response-icon">
              <i class="el-icon-timer"></i>
            </div>
          </div>
          <div class="metric-value">
            <span class="value-number">{{ traffic.avgResponseTime?.toFixed(1) || 0 }}</span>
            <span class="value-unit">ms</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表与列表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 性能趋势图表 -->
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">
              <i class="el-icon-data-line chart-icon"></i>
              系统响应性能趋势 (24h)
            </h3>
          </div>
          <div id="trendChart" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 熔断器状态表 -->
      <el-col :span="8">
        <div class="circuit-card">
          <h3 class="circuit-title">
            <i class="el-icon-connection circuit-icon"></i>
            服务熔断器状态
          </h3>
          
          <div class="circuit-table-container">
            <el-table
              :data="circuitBreakers"
              style="width: 100%"
              :show-header="true"
              class="circuit-table"
              :empty-text="'暂无熔断器数据'">
              <el-table-column
                prop="name"
                label="服务名"
                width="120">
              </el-table-column>
              <el-table-column
                prop="state"
                label="状态"
                width="80">
                <template slot-scope="scope">
                  <el-tag :type="getCircuitStateType(scope.row.state)" size="small">
                    {{ scope.row.state }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="failureCount"
                label="失败次数"
                align="right">
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

// --- 响应式数据定义 ---
const traffic = ref({ qps: 0, errorRate: 0, avgResponseTime: 0 });
const health = ref({ overallStatus: 'LOADING' });
const circuitBreakers = ref([]);

let trendChart = null;

// --- 状态颜色计算属性 ---
const healthTextClass = computed(() => {
  if (health.value.overallStatus === 'HEALTHY') return 'status-healthy';
  if (health.value.overallStatus === 'UNHEALTHY') return 'status-unhealthy';
  if (health.value.overallStatus === 'LOADING') return 'status-loading';
  return 'status-warning';
});

const healthIcon = computed(() => {
  if (health.value.overallStatus === 'HEALTHY') return 'el-icon-success';
  if (health.value.overallStatus === 'UNHEALTHY') return 'el-icon-error';
  return 'el-icon-warning';
});

const healthIconClass = computed(() => {
  if (health.value.overallStatus === 'HEALTHY') return 'icon-healthy';
  if (health.value.overallStatus === 'UNHEALTHY') return 'icon-unhealthy';
  return 'icon-warning';
});

// --- 表格 Badge 样式 ---
const getCircuitStateType = (state) => {
  if (state === 'CLOSED') return 'success';
  if (state === 'OPEN') return 'danger';
  return 'warning'; // HALF_OPEN
};

// --- ECharts 动态更新逻辑 ---
const updateTrendChart = (trendData) => {
  if (!trendChart) return;
  
  const timestamps = trendData?.timestamps || [];
  const responseTimes = trendData?.responseTimes ||[];

  trendChart.setOption({
    xAxis: { data: timestamps },
    series:[{ data: responseTimes }]
  });
};

// --- API 数据获取 ---
const updateAllData = async () => {
  try {
    const [tRes, hRes, cRes, trendRes] = await Promise.all([
      fetch('http://localhost:8080/monitoring/traffic').then(r => r.json()),
      fetch('http://localhost:8080/monitoring/health').then(r => r.json()),
      fetch('http://localhost:8080/monitoring/circuit-breakers').then(r => r.json()),
      fetch('http://localhost:8080/monitoring/performance-trend?hours=24').then(r => r.json())
    ]);

    // 处理Result包装的数据
    traffic.value = tRes.data || tRes;
    health.value = hRes.data || hRes;
    circuitBreakers.value = cRes.data || cRes;

    updateTrendChart(trendRes.data || trendRes);
  } catch (e) {
    console.error("无法获取监控数据，请检查后端服务:", e);
    // 失败时不覆盖已有正常数据，或者视业务需求展示ERROR
  }
};

// --- 初始化高颜值的 Echarts ---
const initTrendChart = () => {
  const chartDom = document.getElementById('trendChart');
  if (!chartDom) return;
  
  // 不使用预设的 'dark' theme，完全自定义以匹配 Tailwind 的 Slate 色系
  trendChart = echarts.init(chartDom);
  const option = {
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.9)', // slate-900
      borderColor: 'rgba(51, 65, 85, 0.5)', // slate-700
      textStyle: { color: '#e2e8f0' }, // slate-200
      axisPointer: { type: 'line', lineStyle: { color: 'rgba(148, 163, 184, 0.3)' } }
    },
    grid: { left: '1%', right: '2%', top: '5%', bottom: '0%', containLabel: true },
    xAxis: { 
      type: 'category', 
      boundaryGap: false, 
      data:[],
      axisLine: { lineStyle: { color: '#334155' } }, // slate-700
      axisLabel: { color: '#94a3b8', margin: 12 } // slate-400
    },
    yAxis: { 
      type: 'value', 
      splitLine: { lineStyle: { color: '#1e293b', type: 'dashed' } }, // slate-800
      axisLabel: { color: '#94a3b8' } 
    },
    series:[{
      name: '响应时间 (ms)',
      type: 'line',
      smooth: 0.4,
      symbol: 'none', // 去掉折线上的圆点使视觉更纯净
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0,[
          { offset: 0, color: '#3b82f6' }, // blue-500
          { offset: 1, color: '#8b5cf6' }  // violet-500
        ])
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1,[
          { offset: 0, color: 'rgba(59, 130, 246, 0.3)' }, // 顶部偏蓝透
          { offset: 1, color: 'rgba(139, 92, 246, 0.01)' } // 底部透明
        ])
      },
      data:[]
    }]
  };
  trendChart.setOption(option);
};

// --- 生命周期钩子 ---
let timer = null;
onMounted(() => {
  initTrendChart();
  updateAllData(); 
  
  timer = setInterval(updateAllData, 5000);
  window.addEventListener('resize', () => trendChart?.resize());
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  window.removeEventListener('resize', () => trendChart?.resize());
});
</script>

<style scoped>
/* 可选：使得滚条更美观 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #475569;
}

/* ================= 全局容器 ================= */
.monitoring-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
  color: #333;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* ================= 顶部标题栏 ================= */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #1890ff;
  font-size: 28px;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
  margin-left: 40px;
}

.header-right {
  display: flex;
  align-items: center;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0f9ff;
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid #d9ecff;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
  position: relative;
}

.live-dot::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: #52c41a;
  border-radius: 50%;
  opacity: 0.3;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.8);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.1;
  }
  100% {
    transform: scale(0.8);
    opacity: 0.3;
  }
}

.live-text {
  font-size: 12px;
  color: #52c41a;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* ================= 指标卡片 ================= */
.metrics-row {
  margin-bottom: 24px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #d9ecff;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.metric-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.metric-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.qps-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.value-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
}

.value-unit {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

/* 健康状态卡片 */
.icon-healthy {
  background: #f6ffed;
  color: #52c41a;
}

.icon-unhealthy {
  background: #fff1f0;
  color: #ff4d4f;
}

.icon-warning {
  background: #fff7e6;
  color: #faad14;
}

.status-healthy {
  color: #52c41a !important;
}

.status-unhealthy {
  color: #ff4d4f !important;
}

.status-loading {
  color: #999 !important;
}

.status-warning {
  color: #faad14 !important;
}

.status-text {
  font-size: 16px;
  font-weight: 600;
}

/* 错误率卡片 */
.error-icon {
  background: #fff1f0;
  color: #ff4d4f;
}

.error-number {
  color: #ff4d4f;
}

/* 响应时间卡片 */
.response-icon {
  background: #f9f0ff;
  color: #722ed1;
}

/* ================= 图表区域 ================= */
.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-icon {
  color: #999;
  font-size: 16px;
}

.chart-container {
  flex: 1;
  min-height: 0;
}

/* ================= 熔断器卡片 ================= */
.circuit-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  height: 400px;
  display: flex;
  flex-direction: column;
}

.circuit-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.circuit-icon {
  color: #999;
  font-size: 16px;
}

.circuit-table-container {
  flex: 1;
  overflow: hidden;
}

/* Element UI 表格样式覆盖 - 简约版本 */
.el-table {
  background: #ffffff !important;
  color: #333 !important;
}

.el-table::before {
  display: none !important;
}

.el-table th,
.el-table td {
  background: #ffffff !important;
  border-bottom: 1px solid #f0f0f0 !important;
  color: #333 !important;
  padding: 12px 8px !important;
}

.el-table th {
  color: #666 !important;
  font-weight: 600 !important;
  border-bottom-color: #e5e7eb !important;
  background: #fafafa !important;
}

.el-table .el-table__row {
  background: #ffffff !important;
}

.el-table .el-table__row:hover td {
  background: #f5f7fa !important;
}

.el-table .el-table__empty-block {
  background: #ffffff !important;
}

.el-table .el-table__empty-text {
  color: #999 !important;
}

/* Element UI Tag 样式覆盖 - 简约版本 */
.el-tag {
  background: #f0f0f0 !important;
  border: 1px solid #d9d9d9 !important;
  color: #666 !important;
}

.el-tag.el-tag--success {
  background: #f6ffed !important;
  color: #52c41a !important;
  border-color: #b7eb8f !important;
}

.el-tag.el-tag--danger {
  background: #fff1f0 !important;
  color: #ff4d4f !important;
  border-color: #ffccc7 !important;
}

.el-tag.el-tag--warning {
  background: #fff7e6 !important;
  color: #faad14 !important;
  border-color: #ffe58f !important;
}

.el-tag.el-tag--small {
  padding: 2px 6px !important;
  font-size: 12px !important;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f5f7fa;
}

::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>