<template>
  <div>
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="行为类型">
          <el-select v-model="filter.actionType" placeholder="全部" clearable style="width:180px">
            <el-option label="咨询老师" value="consult_teacher" />
            <el-option label="咨询老师(购课页)" value="consult_teacher_buy" />
            <el-option label="试听点击" value="trial_from_buy" />
            <el-option label="购买点击" value="buy_from_buy" />
            <el-option label="加入学习群" value="scan_group" />
            <el-option label="复制微信号" value="copy_wechat" />
            <el-option label="领取资料" value="claim_material" />
            <el-option label="查看课程" value="view_course" />
            <el-option label="课程试听" value="course_trial" />
            <el-option label="课程购买" value="course_buy" />
          </el-select>
        </el-form-item>
        <el-form-item label="OpenID">
          <el-input v-model="filter.openid" placeholder="搜索 OpenID" clearable style="width:180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>用户行为记录（共 {{ total }} 条）</span>
          <div>
            <el-tag v-for="(count, type) in actionSummary" :key="type" style="margin-left:8px">
              {{ actionLabel(type) }}: {{ count }}
            </el-tag>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="openid" label="OpenID" width="180" show-overflow-tooltip />
        <el-table-column prop="actionType" label="行为类型" width="180">
          <template #default="{ row }">
            <el-tag :type="actionTagType(row.actionType)" size="small">
              {{ actionLabel(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="extra" label="附加信息" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="160" />
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end;display:flex"
        :current-page="page" :page-size="size" :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </el-card>
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
const filter = ref({ actionType: '', openid: '' })

const actionSummary = computed(() => {
  const map = {}
  list.value.forEach(item => {
    map[item.actionType] = (map[item.actionType] || 0) + 1
  })
  return map
})

const ACTION_LABELS = {
  consult_teacher: '咨询老师',
  consult_teacher_buy: '咨询(购课页)',
  trial_from_buy: '试听点击',
  buy_from_buy: '购买点击',
  scan_group: '加入学习群',
  copy_wechat: '复制微信号',
  claim_material: '领取资料',
  view_course: '查看课程',
  course_trial: '课程试听',
  course_buy: '课程购买',
  join_group_from_buy: '进群试听',
  save_teacher_qrcode: '保存老师二维码',
  save_group_qrcode: '保存群二维码'
}

function actionLabel(type) {
  return ACTION_LABELS[type] || type
}

function actionTagType(type) {
  if (['buy_from_buy', 'course_buy'].includes(type)) return 'danger'
  if (['trial_from_buy', 'course_trial', 'view_course'].includes(type)) return 'warning'
  if (['consult_teacher', 'consult_teacher_buy', 'copy_wechat'].includes(type)) return 'primary'
  return 'info'
}

async function load(p = page.value) {
  page.value = p
  loading.value = true
  try {
    const res = await adminApi.getActions({
      page: p, size: size.value,
      actionType: filter.value.actionType || undefined,
      openid: filter.value.openid || undefined
    })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.value = { actionType: '', openid: '' }
  load(1)
}

onMounted(() => load())
</script>

<style scoped>
.filter-card { margin-bottom: 0; }
</style>
