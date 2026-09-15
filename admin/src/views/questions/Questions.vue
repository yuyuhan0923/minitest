<template>
  <div>
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="分类">
          <el-select v-model="query.category" clearable placeholder="全部分类" style="width:140px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="搜索题目" clearable style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="openDialog()">新增题目</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="correctAnswer" label="答案" width="80" align="center" />
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRow(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editRow ? '编辑题目' : '新增题目'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="题目" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="选项" prop="optionsJson">
          <div v-for="(opt, idx) in optionList" :key="idx" class="option-row">
            <el-tag>{{ opt.key }}</el-tag>
            <el-input v-model="opt.text" :placeholder="`选项${opt.key}`" style="flex:1;margin:0 8px" />
          </div>
        </el-form-item>
        <el-form-item label="正确答案" prop="correctAnswer">
          <el-radio-group v-model="form.correctAnswer">
            <el-radio v-for="opt in optionList" :key="opt.key" :value="opt.key">{{ opt.key }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :max="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const categories = ['机器学习', '深度学习', '数据处理', '算法基础']
const list = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editRow = ref(null)
const formRef = ref()

const query = reactive({ page: 1, size: 20, category: '', keyword: '' })
const form = reactive({ title: '', category: '', correctAnswer: 'A', score: 5, sortOrder: 0, status: 1 })
const optionList = ref([
  { key: 'A', text: '' }, { key: 'B', text: '' },
  { key: 'C', text: '' }, { key: 'D', text: '' }
])

const rules = {
  title: [{ required: true, message: '请输入题目', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  correctAnswer: [{ required: true, message: '请选择正确答案', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getQuestions(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { page: 1, category: '', keyword: '' })
  loadData()
}

function openDialog(row = null) {
  editRow.value = row
  if (row) {
    Object.assign(form, row)
    try {
      const opts = JSON.parse(row.optionsJson)
      optionList.value = opts
    } catch {}
  } else {
    Object.assign(form, { title: '', category: '', correctAnswer: 'A', score: 5, sortOrder: 0, status: 1 })
    optionList.value = [{ key: 'A', text: '' }, { key: 'B', text: '' }, { key: 'C', text: '' }, { key: 'D', text: '' }]
  }
  dialogVisible.value = true
}

async function saveQuestion() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { ...form, optionsJson: JSON.stringify(optionList.value) }
    if (editRow.value) {
      await adminApi.updateQuestion(editRow.value.id, data)
    } else {
      await adminApi.createQuestion(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function deleteRow(row) {
  await ElMessageBox.confirm(`确认删除题目「${row.title.slice(0, 20)}...」？`, '警告', { type: 'warning' })
  await adminApi.deleteQuestion(row.id)
  ElMessage.success('删除成功')
  loadData()
}

loadData()
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.option-row { display: flex; align-items: center; margin-bottom: 8px; }
</style>
