<template>
  <div>
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="搜索">
          <el-input v-model="keyword" placeholder="昵称 / 手机号 / OpenID" clearable style="width:220px" />
        </el-form-item>
        <el-form-item label="行为标签">
          <el-select v-model="filterTag" clearable placeholder="全部" style="width:110px">
            <el-option label="活跃" value="活跃" />
            <el-option label="新用户" value="新用户" />
            <el-option label="沉默" value="沉默" />
          </el-select>
        </el-form-item>
        <el-form-item label="测评等级">
          <el-select v-model="filterLevel" clearable placeholder="全部" style="width:110px">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>
        <span>用户管理（共 {{ filtered.length }} 人）</span>
      </template>

      <el-table
        :data="paged"
        v-loading="loading"
        stripe
        highlight-current-row
        @row-click="openDetail"
        style="cursor:pointer"
      >
        <el-table-column prop="openid" label="OpenID" width="180" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" width="110" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="assessLevel" label="测评等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelType(row.assessLevel)" size="small">{{ row.assessLevel || '初级' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="behaviorTag" label="行为标签" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.behaviorTag)" size="small">{{ row.behaviorTag || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="todayExams" label="今日测评" width="90" align="center">
          <template #default="{ row }">
            <el-badge v-if="row.todayExams > 0" :value="row.todayExams" type="primary" />
            <span v-else class="text-gray">0</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalExams" label="总测评" width="80" align="center" />
        <el-table-column prop="hasLead" label="留资状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.hasLead === 1 ? 'success' : 'info'" size="small">
              {{ row.hasLead === 1 ? '已留资' : '未留资' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top:16px;justify-content:flex-end;display:flex"
        :current-page="page"
        :page-size="size"
        :total="filtered.length"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        @current-change="p => page = p"
        @size-change="s => { size = s; page = 1 }"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="用户详情" width="480px" destroy-on-close>
      <div v-if="detailLoading" style="text-align:center;padding:32px">
        <el-icon class="is-loading" size="28"><Loading /></el-icon>
      </div>
      <el-descriptions v-else-if="detail" :column="1" border>
        <el-descriptions-item label="OpenID">{{ detail.openid }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="测评等级">
          <el-tag :type="levelType(detail.assessLevel)" size="small">{{ detail.assessLevel || '初级' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="行为标签">
          <el-tag :type="tagType(detail.behaviorTag)" size="small">{{ detail.behaviorTag || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ fmt(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="今日测评">{{ detail.todayExams }}</el-descriptions-item>
        <el-descriptions-item label="总测评次数">{{ detail.totalExams }}</el-descriptions-item>
        <el-descriptions-item label="留资状态">
          <el-tag :type="detail.hasLead === 1 ? 'success' : 'info'" size="small">
            {{ detail.hasLead === 1 ? '已留资' : '未留资' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { adminApi } from '@/api/index'

const all = ref([])
const loading = ref(false)

const keyword = ref('')
const filterTag = ref('')
const filterLevel = ref('')
const activeKeyword = ref('')
const activeTag = ref('')
const activeLevel = ref('')

const page = ref(1)
const size = ref(20)

const filtered = computed(() => {
  return all.value.filter(u => {
    const kw = activeKeyword.value.trim().toLowerCase()
    if (kw) {
      const hit = (u.nickname || '').toLowerCase().includes(kw)
        || (u.phone || '').includes(kw)
        || (u.openid || '').toLowerCase().includes(kw)
      if (!hit) return false
    }
    if (activeTag.value && u.behaviorTag !== activeTag.value) return false
    if (activeLevel.value && u.assessLevel !== activeLevel.value) return false
    return true
  })
})

const paged = computed(() => {
  const start = (page.value - 1) * size.value
  return filtered.value.slice(start, start + size.value)
})

async function load() {
  loading.value = true
  try {
    console.log('[AllUsers] 请求 GET /api/admin/users')
    const res = await adminApi.getUsers()
    all.value = Array.isArray(res) ? res : (res.list || [])
    console.log('[AllUsers] 加载成功，用户数：', all.value.length)
  } catch (err) {
    console.error('[AllUsers] 加载失败：', err)
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  activeKeyword.value = keyword.value
  activeTag.value = filterTag.value
  activeLevel.value = filterLevel.value
  page.value = 1
}

function resetFilter() {
  keyword.value = ''
  filterTag.value = ''
  filterLevel.value = ''
  activeKeyword.value = ''
  activeTag.value = ''
  activeLevel.value = ''
  page.value = 1
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    console.log('[AllUsers] 请求用户详情 openid：', row.openid)
    detail.value = await adminApi.getUserDetail(row.openid)
    console.log('[AllUsers] 详情加载成功：', detail.value)
  } catch (err) {
    console.error('[AllUsers] 详情加载失败：', err)
  } finally {
    detailLoading.value = false
  }
}

function levelType(level) {
  if (level === '高级') return 'danger'
  if (level === '中级') return 'warning'
  return 'info'
}

function tagType(tag) {
  if (tag === '活跃') return 'success'
  if (tag === '沉默') return 'info'
  return 'warning'
}

function fmt(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').substring(0, 16)
}

onMounted(load)
</script>

<style scoped>
.filter-card { margin-bottom: 0; }
.text-gray { color: #999; }
</style>
