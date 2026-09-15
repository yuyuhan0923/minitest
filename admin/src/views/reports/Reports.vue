<template>
  <div>
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="能力等级">
          <el-select v-model="query.levelCode" clearable placeholder="全部等级" style="width:130px">
            <el-option label="入门级" value="beginner" />
            <el-option label="进阶级" value="intermediate" />
            <el-option label="专家级" value="expert" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="openid" label="OpenID" width="200" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelType(row.levelCode)" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="综合评价" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="测评时间" width="160" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
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

    <!-- 报告详情弹窗 -->
    <el-dialog v-model="detailVisible" title="测评报告详情" width="600px">
      <div v-if="currentReport" class="report-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总分">{{ currentReport.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag :type="levelType(currentReport.levelCode)">{{ currentReport.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="综合评价" :span="2">{{ currentReport.summary }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">各方向得分</div>
        <div v-for="cs in parsedCategoryScores" :key="cs.category" class="cat-item">
          <div class="cat-header">
            <span>{{ cs.category }}</span>
            <span>{{ cs.score }}/{{ cs.total }} ({{ cs.percentage }}%)</span>
          </div>
          <el-progress :percentage="cs.percentage" :stroke-width="10" />
        </div>

        <div class="section-title">提升建议</div>
        <ul class="suggest-list">
          <li v-for="(s, i) in parsedSuggestions" :key="i">{{ s }}</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { adminApi } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const currentReport = ref(null)
const query = reactive({ page: 1, size: 20, levelCode: '' })

function levelType(code) {
  if (code === 'expert') return 'success'
  if (code === 'intermediate') return 'warning'
  return 'info'
}

const parsedCategoryScores = computed(() => {
  if (!currentReport.value) return []
  try { return JSON.parse(currentReport.value.categoryScoresJson) } catch { return [] }
})

const parsedSuggestions = computed(() => {
  if (!currentReport.value) return []
  try { return JSON.parse(currentReport.value.suggestionsJson) } catch { return [] }
})

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getReports(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { page: 1, levelCode: '' })
  loadData()
}

function viewDetail(row) {
  currentReport.value = row
  detailVisible.value = true
}

loadData()
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.report-detail { padding: 0 4px; }
.section-title { font-size: 14px; font-weight: 600; color: #333; margin: 20px 0 12px; }
.cat-item { margin-bottom: 16px; }
.cat-header { display: flex; justify-content: space-between; font-size: 13px; color: #555; margin-bottom: 6px; }
.suggest-list { padding-left: 20px; margin: 0; }
.suggest-list li { font-size: 13px; color: #555; line-height: 1.8; }
</style>
