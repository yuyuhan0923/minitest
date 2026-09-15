<template>
  <div>
    <el-card v-loading="loading">
      <template #header>
        <span>课程配置（只读，修改请编辑 application.yml）</span>
      </template>

      <el-descriptions v-if="config" :column="1" border label-width="140px">
        <el-descriptions-item label="课程名称">{{ config.courseName }}</el-descriptions-item>
        <el-descriptions-item label="当前价格">
          <span style="color:#ef4444;font-weight:700;font-size:16px">{{ config.coursePrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="课程简介">{{ config.courseDiscount }}</el-descriptions-item>
        <el-descriptions-item label="老师微信号">{{ config.teacherWechatId }}</el-descriptions-item>
        <el-descriptions-item label="购买链接">
          <el-link v-if="config.courseUrl" :href="config.courseUrl" target="_blank" type="primary">
            {{ config.courseUrl }}
          </el-link>
          <span v-else class="text-gray">未配置</span>
        </el-descriptions-item>
        <el-descriptions-item label="试听链接">
          <el-link v-if="config.buyUrl" :href="config.buyUrl" target="_blank" type="primary">
            {{ config.buyUrl }}
          </el-link>
          <span v-else class="text-gray">未配置</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-empty v-else-if="!loading" description="暂无配置数据" />
    </el-card>

    <el-card style="margin-top:16px">
      <template #header><span>配置说明</span></template>
      <el-alert type="info" :closable="false">
        <p>课程配置在 <code>backend/src/main/resources/application.yml</code> 的 <code>app.course.*</code> 节点下维护。</p>
        <p style="margin-top:8px">修改后需重启后端服务生效。</p>
      </el-alert>
      <el-descriptions style="margin-top:16px" :column="1" border label-width="200px">
        <el-descriptions-item label="app.course.course-name">课程名称</el-descriptions-item>
        <el-descriptions-item label="app.course.course-price">当前售价文案</el-descriptions-item>
        <el-descriptions-item label="app.course.course-discount">课程简介/卖点</el-descriptions-item>
        <el-descriptions-item label="app.course.course-url">购买页链接（小鹅通等）</el-descriptions-item>
        <el-descriptions-item label="app.course.buy-url">试听页链接</el-descriptions-item>
        <el-descriptions-item label="app.wechat.teacher-wechat-id">老师微信号</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/index'

const config = ref(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    config.value = await adminApi.getCourseConfig()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.text-gray { color: #999; }
code { background: #f5f7fa; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
</style>
