<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <el-card class="stat-card" shadow="never">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: card.bg }">{{ card.icon }}</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview[card.key] ?? '-' }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
          <div class="stat-today" v-if="card.todayKey">
            今日 +{{ overview[card.todayKey] ?? 0 }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>近7日趋势</span>
              <el-radio-group v-model="chartDays" size="small" @change="loadDailyStats">
                <el-radio-button :value="7">7天</el-radio-button>
                <el-radio-button :value="14">14天</el-radio-button>
                <el-radio-button :value="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="chartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>转化漏斗</span></template>
          <div class="funnel">
            <div class="funnel-item" v-for="item in funnelData" :key="item.label">
              <div class="funnel-bar-wrap">
                <div class="funnel-bar" :style="{ width: item.pct + '%', background: item.color }"></div>
              </div>
              <div class="funnel-info">
                <span class="funnel-label">{{ item.label }}</span>
                <span class="funnel-value">{{ item.value }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api'

const overview = ref({})
const dailyStats = ref([])
const chartRef = ref()
const chartDays = ref(7)
let chart = null

const statCards = [
  { key: 'registerUsers', todayKey: 'todayUsers', label: '注册用户', icon: '👤', bg: 'rgba(102,126,234,0.1)' },
  { key: 'totalLeads', todayKey: 'todayLeads', label: '留资人数', icon: '📩', bg: 'rgba(16,185,129,0.1)' },
  { key: 'visitUsers', label: '访问人数', icon: '👁️', bg: 'rgba(245,158,11,0.1)' },
  { key: 'completeExams', todayKey: 'todayExams', label: '完成测评', icon: '✅', bg: 'rgba(239,68,68,0.1)' },
  { key: 'conversionRate', label: '转化率(%)', icon: '📈', bg: 'rgba(102,126,234,0.1)' },
  { key: 'buyClickUsers', label: '点击购买', icon: '🛒', bg: 'rgba(245,158,11,0.1)' }
]

const funnelData = computed(() => {
  const users = overview.value.registerUsers || 0
  const exams = overview.value.completeExams || 0
  const leads = overview.value.totalLeads || 0
  return [
    { label: '访问用户', value: users, pct: 100, color: '#667eea' },
    { label: '完成测评', value: exams, pct: users > 0 ? Math.round(exams / users * 100) : 0, color: '#764ba2' },
    { label: '留资用户', value: leads, pct: exams > 0 ? Math.round(leads / exams * 100) : 0, color: '#10b981' }
  ]
})

async function loadOverview() {
  let res = await adminApi.getOverview()
  console.log("后端返回数据：", res) 
  overview.value = res
}

async function loadDailyStats() {
  dailyStats.value = await adminApi.getDailyStats(chartDays.value)
  renderChart()
}

function renderChart() {
  if (!chart) chart = echarts.init(chartRef.value)
  const dates = dailyStats.value.map(d => d.date.slice(5))
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['答题数', '留资数'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e0e0e0' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [
      {
        name: '答题数', type: 'line', smooth: true,
        data: dailyStats.value.map(d => d.exams),
        itemStyle: { color: '#667eea' },
        areaStyle: { color: 'rgba(102,126,234,0.1)' }
      },
      {
        name: '留资数', type: 'line', smooth: true,
        data: dailyStats.value.map(d => d.leads),
        itemStyle: { color: '#10b981' },
        areaStyle: { color: 'rgba(16,185,129,0.1)' }
      }
    ]
  })
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadDailyStats()])
})
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-row { margin-bottom: 20px; }
.stat-card { border-radius: 12px; }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; font-size: 24px; display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.stat-today { font-size: 12px; color: #10b981; margin-top: 12px; }
.chart-row { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.funnel { padding: 10px 0; }
.funnel-item { margin-bottom: 20px; }
.funnel-bar-wrap { height: 12px; background: #f0f0f5; border-radius: 6px; overflow: hidden; margin-bottom: 8px; }
.funnel-bar { height: 100%; border-radius: 6px; transition: width 0.6s; }
.funnel-info { display: flex; justify-content: space-between; font-size: 13px; }
.funnel-label { color: #666; }
.funnel-value { font-weight: 600; color: #333; }
</style>