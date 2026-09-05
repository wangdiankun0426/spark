<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="skill-header">
      <div class="skill-header-left">
        <div class="skill-header-title">
          <el-icon class="skill-header-icon">
            <component :is="Skill" />
          </el-icon>
          技能库
        </div>
        <div class="skill-header-subtitle">浏览平台沉淀的技能，智能体命中技能时将按其指令执行</div>
      </div>
      <div class="skill-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索技能名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button
            type="primary"
            @click="handleOpenCreateForm"
        ><el-icon><Plus /></el-icon>新增技能
        </el-button>
      </div>
    </div>

    <!-- 技能卡片网格 -->
    <div
        class="skill-grid"
        v-if="skills.length"
    >
      <info-card
          v-for="skill in skills"
          :key="skill.id"
          :theme="getTheme(skill)"
          :icon="Skill"
          :title="skill.name"
          :id-text="'编号 #' + skill.id"
          :description="skill.description || '暂无描述'"
          :disabled="skill.status !== 1"
          :height="220"
          @click="handleOpenSkill(skill)"
      >
        <!-- 状态徽章 -->
        <template #badge>
          <span
              class="badge"
              :class="skill.status === 1 ? 'badge-primary' : 'badge-muted'"
          >{{ skill.statusName }}
          </span>
        </template>
        <!-- 底部操作-->
        <template #action>
          <span class="action-item action-edit" @click.stop="handleOpenUpdateForm(skill)">修改</span>
          <span class="action-item action-danger" @click.stop="handleDelete(skill)">删除</span>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的技能' : '暂无技能'"
        :image-size="120"
    />
    <!-- 分页 -->
    <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />

    <!-- 技能详情抽屉 -->
    <el-drawer
        v-model="drawerVisible"
        :title="currentSkill?.name || ''"
        direction="ltr"
        size="50%"
    >
      <div v-loading="loading">
        <template v-if="currentSkill">
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="编号">{{ currentSkill.id }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ currentSkill.statusName }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentSkill.description }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">技能内容</el-divider>
          <v-md-preview v-if="currentSkill.content" :text="currentSkill.content"/>
          <el-empty v-else description="该技能暂无内容" :image-size="80"/>
        </template>
      </div>
    </el-drawer>

    <!-- 新增 / 修改 技能表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="60%"
        :before-close="handleCloseForm"
    >
      <el-form
          :model="form"
          label-width="auto"
          :rules="formRules"
          ref="formRef"
      >
        <el-form-item label="名称" prop="name">
          <el-input
              v-model="form.name"
              placeholder="请输入技能名称，如：SQL编写规范"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="form.description"
              placeholder="请输入一句话描述，用于装配目录供模型判断是否命中该技能"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="技能内容" prop="content">
          <v-md-editor
              v-model="form.content"
              height="480px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
      </el-form>
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>名称为必填项且全局唯一，是模型调用读取技能时的定位依据；</div>
            <div>描述建议写清该技能的适用场景与触发条件，将注入智能体装配目录用于命中判断；</div>
            <div>技能内容为 Markdown 指令正文，智能体命中该技能时会读取全文并严格按其执行；</div>
            <div>停用的技能不会被智能体装配目录收录。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitForm">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageSkillListAPI,
  querySkillDetailAPI,
  createSkillAPI,
  updateSkillAPI,
  deleteSkillAPI
} from '@/api/llm/skill.js'
import { Search, Plus } from '@element-plus/icons-vue'
import Skill from "@/assets/icons/skill.vue";
import InfoCard from '@/components/InfoCard/index.vue'

const skills = ref([])
const total = ref(0)
const pageSizes = [10, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
})

const drawerVisible = ref(false)
const loading = ref(false)
const currentSkill = ref(null)

// 新增 / 修改 表单
const formVisible = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const form = ref({
  id: undefined,
  name: undefined,
  description: undefined,
  content: undefined,
  status: 1,
})
const formRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入技能名称' }],
  description: [{ required: true, trigger: 'blur', message: '请输入技能描述' }],
  content: [{ required: true, trigger: 'blur', message: '请输入技能内容' }],
}

// 主题色循环，与 agent 卡片保持一致
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

let searchTimer = null

onMounted(() => {
  loadSkills()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后回到第一页并重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1
    loadSkills()
  }, 300)
})

/**
 * 分页查询技能列表，携带名称关键字
 */
function loadSkills() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value
  }
  pageSkillListAPI(params).then(res => {
    if (res.data && res.data.rows) {
      skills.value = res.data.rows
      total.value = res.data.total || 0
    }
  })
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  loadSkills()
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadSkills()
}

/**
 * 根据 skill.id 计算主题名
 * @param skill
 */
function getTheme(skill) {
  return themes[skill.id % themes.length]
}

/**
 * 打开技能详情抽屉，拉取技能指令全文
 * @param skill
 */
function handleOpenSkill(skill) {
  drawerVisible.value = true
  loading.value = true
  currentSkill.value = skill
  querySkillDetailAPI({ id: skill.id }).then(res => {
    if (res.data) {
      currentSkill.value = res.data
    }
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 重置表单为默认值
 */
function resetForm() {
  form.value.id = undefined
  form.value.name = undefined
  form.value.description = undefined
  form.value.content = undefined
  form.value.status = 1
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  resetForm()
  formTitle.value = '新增技能'
  formVisible.value = true
}

/**
 * 打开修改表单（拉取技能全文回填）
 * @param skill
 */
function handleOpenUpdateForm(skill) {
  querySkillDetailAPI({ id: skill.id }).then(res => {
    if (!res.data) return
    form.value.id = res.data.id
    form.value.name = res.data.name
    form.value.description = res.data.description
    form.value.content = res.data.content
    form.value.status = res.data.status
    formTitle.value = '修改技能'
    formVisible.value = true
  })
}

/**
 * 关闭表单
 */
function handleCloseForm() {
  resetForm()
  formTitle.value = ''
  formVisible.value = false
}

/**
 * 提交表单（新增 / 修改）
 */
function handleSubmitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    const data = {
      id: form.value.id,
      name: form.value.name,
      description: form.value.description,
      content: form.value.content,
      status: form.value.status,
    }
    if (!data.id) {
      createSkillAPI(data).then(res => {
        if (res.code !== 200) return
        ElMessage.success('技能创建成功')
        handleCloseForm()
        loadSkills()
      })
    } else {
      updateSkillAPI(data).then(res => {
        if (res.code !== 200) return
        ElMessage.success('技能修改成功')
        handleCloseForm()
        loadSkills()
      })
    }
  })
}

/**
 * 删除技能
 * @param skill
 */
function handleDelete(skill) {
  ElMessageBox.confirm('是否确定删除此条技能?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteSkillAPI({ id: skill.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('删除技能成功')
      // 删除当前页最后一条时回退上一页，避免停留在空页
      if (skills.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1
      }
      loadSkills()
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.skill-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.skill-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.skill-header-icon {
  font-size: 24px;
  color: $color-primary;
}

.skill-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.skill-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.skill-grid {
  height: calc(100vh - #{$nav-height} - 190px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
  :deep(.info-card-action) {
    gap: $spacing-sm;
    .action-edit {
      color: var(--el-color-warning);
    }
    .action-danger {
      color: var(--el-color-danger);
    }
  }
}

.empty-grid {
  height: calc(100vh - #{$nav-height} - 190px);
}

.drawer-footer {
  padding: 0 $spacing-md;
}

.form-tip {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}
</style>
