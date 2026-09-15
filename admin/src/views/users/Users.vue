<template>
  <div>
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="搜索手机号" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="跟进状态">
          <el-select v-model="query.followStatus" clearable placeholder="全部" style="width:120px">
            <el-option label="未跟进" :value="0" />
            <el-option label="跟进中" :value="1" />
            <el-option label="已转化" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="exportLeads">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip />
        <el-table-column prop="level" label="测评等级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.level" :type="levelType(row.level)" size="small">{{ row.level }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="得分" width="80" align="center" />
        <el-table-column prop="followStatus" label="跟进状态" width="110" align="center">
          <template #default="{ row }">
            <el-select v-model="row.followStatus" size="small" @change="updateFollow(row)">
              <el-option label="未跟进" :value="0" />
              <el-option label="跟进中" :value="1" />
              <el-option label="已转化" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column prop="createdAt" label="留资时间" width="160" />
      </el-table>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @change="loadData"
        style="margin-top:16px; justify-content:flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 20, phone: '', followStatus: null })

function levelType(level) {
  if (level === '专家级') return 'success'
  if (level === '进阶级') return 'warning'
  return 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getLeads(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { page: 1, phone: '', followStatus: null })
  loadData()
}

async function updateFollow(row) {
  await adminApi.updateFollowStatus(row.id, { followStatus: row.followStatus })
  ElMessage.success('状态已更新')
}

function exportLeads() {
  // 简单CSV导出
  const headers = ['ID', '手机号', '昵称', '测评等级', '得分', '跟进状态', '留资时间']
  const statusMap = { 0: '未跟进', 1: '跟进中', 2: '已转化' }
  const rows = list.value.map(r => [
    r.id, r.phone, r.nickname || '', r.level || '', r.totalScore || '',
    statusMap[r.followStatus], r.createdAt
  ])
  const csv = [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `线索数据_${new Date().toLocaleDateString()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

loadData()
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
</style>
