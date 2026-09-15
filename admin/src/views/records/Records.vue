<template>
  <div>
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="OpenID">
          <el-input v-model="filter.openid" placeholder="搜索 OpenID" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="filter.level" placeholder="全部" clearable style="width:120px">
            <el-option label="入门级" value="入门级" />
            <el-option label="进阶级" value="进阶级" />
            <el-option label="专家级" value="专家级" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>
        <span>测评记录（共 {{ total }} 条）</span>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="openid" label="OpenID" width="180" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="得分" width="80" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="综合评价" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="测评时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end;display:flex"
        :current-page="page" :page-size="size" :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="测评详情" width="600px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="OpenID">{{ current.openid }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ current.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag :type="levelTagType(current.level)" size="small">{{ current.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="测评时间">{{ current.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="综合评价" :span="2">{{ current.summary }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top:16px">
          <div class="detail-title">各方向得分</div>
          <div v-for="cat in parsedCategoryScores" :key="cat.category" style="margin-bottom:10px">
            <div style="display:flex;justify-content:space-between;margin-bottom:4px">
              <span>{{ cat.category }}</span>
              <span style="color:#667eea">{{ cat.score }}/{{ cat.total }}</span>
            </div>
            <el-progress :percentage="cat.percentage" :stroke-width="8" />
          </div>
        </div>

        <div style="margin-top:16px">
          <div class="detail-title">提升建议</div>
          <div v-for="(s, i) in parsedSuggestions" :key="i" class="suggest-item">
            💡 {{ s }}
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/index'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const filter = ref({ openid: '', level: '' })
const detailVisible = ref(false)
const current = ref(null)

const parsedCategoryScores = computed(() => {
  if (!current.value?.categoryScoresJson) return []
  try { return JSON.parse(current.value.categoryScoresJson) } catch { return [] }
})
const parsedSuggestions = computed(() => {
  if (!current.value?.suggestionsJson) return []
  try { return JSON.parse(current.value.suggestionsJson) } catch { return [] }
})

async function load(p = page.value) {
  page.value = p
  loading.value = true
  try {
    const res = await adminApi.getRecords({
      page: p, size: size.value,
      openid: filter.value.openid || undefined,
      level: filter.value.level || undefined
    })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.value = { openid: '', level: '' }
  load(1)
}

function showDetail(row) {
  current.value = row
  detailVisible.value = true
}

function levelTagType(level) {
  if (!level) return 'info'
  if (level.includes('专家')) return 'danger'
  if (level.includes('进阶')) return 'warning'
  return 'info'
}

onMounted(() => load())
</script>

<style scoped>
.filter-card { margin-bottom: 0; }
.detail-title { font-weight: 600; color: #1a1a2e; margin-bottom: 10px; }
.suggest-item { font-size: 13px; color: #555; line-height: 1.8; }
</style>
