<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="skill-header">
      <div class="skill-header-left">
        <div class="skill-header-title">
          技能库
        </div>
        <div class="skill-header-subtitle">管理沉淀的技能，智能体命中技能时将按其指令执行</div>
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
          :icon="Skill"
          :title="skill.name"
          :description="skill.description || '暂无描述'"
          :disabled="skill.status !== 1"
          :actions="cardActions(skill)"
      />
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

    <!-- 新增 / 修改 技能表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="70%"
        :before-close="handleCloseForm"
        :close-on-click-modal="false"
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
        <el-form-item label="技能文件" prop="content">
          <div class="skill-file-layout">
            <div class="skill-file-panel">
              <div
                  class="skill-file-item"
                  :class="{ active: activeFile === MAIN_FILE }"
                  @click="handleSwitchFile(MAIN_FILE)"
              >
                <span class="skill-file-name">SKILL.md</span>
                <span class="skill-file-tag">主文件</span>
              </div>
              <div
                  v-for="file in form.files"
                  :key="file.filePath"
                  class="skill-file-item"
                  :class="{ active: activeFile === file.filePath }"
                  @click="handleSwitchFile(file.filePath)"
              >
                <span
                    class="skill-file-name"
                    :title="file.description"
                >{{ file.filePath }}</span>
                <el-icon
                    class="skill-file-delete"
                    @click.stop="handleRemoveFile(file.filePath)"
                ><Close /></el-icon>
              </div>
              <div
                  class="skill-file-add"
                  @click="handleOpenFileDialog"
              >
                <el-icon><Plus /></el-icon>添加文件
              </div>
            </div>
            <v-md-editor
                v-model="editorContent"
                class="skill-file-editor"
                height="500px"
            />
          </div>
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
            <div>SKILL.md 为主文件，只写精简的执行骨架与何时读取哪个子文件的指引，细节内容拆到子文件中；</div>
            <div>子文件可放 references/ 参考文档、scripts/ 脚本等，其用途描述会注入资源清单，模型按需读取全文；</div>
            <div>脚本类文件仅作为模型的参考资源，不会在服务端真正执行；</div>
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

    <!-- 添加技能文件弹窗 -->
    <el-dialog
        v-model="fileDialogVisible"
        title="添加技能文件"
        width="480px"
        append-to-body
        :close-on-click-modal="false"
    >
      <el-form
          :model="fileForm"
          label-width="auto"
          :rules="fileFormRules"
          ref="fileFormRef"
      >
        <el-form-item label="文件路径" prop="filePath">
          <el-input
              v-model="fileForm.filePath"
              placeholder="如 references/api.md、scripts/query.sql"
              maxlength="255"
          />
        </el-form-item>
        <el-form-item label="用途描述" prop="description">
          <el-input
              v-model="fileForm.description"
              placeholder="说明该文件的用途，供模型判断何时读取"
              type="textarea"
              :rows="2"
              maxlength="255"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleConfirmAddFile">确定</el-button>
        <el-button @click="fileDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageSkillListAPI,
  querySkillDetailAPI,
  createSkillAPI,
  updateSkillAPI,
  deleteSkillAPI
} from '@/api/llm/skill.js'
import { Search, Plus, Close } from '@element-plus/icons-vue'
import Skill from "@/assets/icons/skill.vue";
import InfoCard from '@/components/InfoCard/index.vue'

const skills = ref([])
const total = ref(0)
const pageSizes = [15, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 15,
})

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
  files: [],
})
const formRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入技能名称' }],
  description: [{ required: true, trigger: 'blur', message: '请输入技能描述' }],
  content: [{ required: true, trigger: 'blur', message: '请输入主文件内容' }],
}

// 主文件在文件列表面板中的固定标识
const MAIN_FILE = 'SKILL.md'
const activeFile = ref(MAIN_FILE)
// 编辑器内容需随当前选中文件读写，v-model 无法直接绑到动态目标
const editorContent = computed({
  get() {
    if (activeFile.value === MAIN_FILE) {
      return form.value.content
    }
    const file = form.value.files.find(f => f.filePath === activeFile.value)
    return file ? file.content : ''
  },
  set(val) {
    if (activeFile.value === MAIN_FILE) {
      form.value.content = val
      return
    }
    const file = form.value.files.find(f => f.filePath === activeFile.value)
    if (file) {
      file.content = val
    }
  }
})

// 添加技能文件弹窗
const fileDialogVisible = ref(false)
const fileFormRef = ref(null)
const fileForm = ref({
  filePath: undefined,
  description: undefined,
})
const fileFormRules = {
  filePath: [{ required: true, trigger: 'blur', message: '请输入文件路径' }],
  description: [{ required: true, trigger: 'blur', message: '请输入用途描述' }],
}

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
 * 卡片底部工具栏按钮
 * @param skill
 */
function cardActions(skill) {
  return [
    { key: 'edit', label: '修改', icon: 'Edit', onClick: () => handleOpenUpdateForm(skill) },
    { key: 'delete', label: '删除', icon: 'Delete', onClick: () => handleDelete(skill) }
  ]
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
  form.value.files = []
  activeFile.value = MAIN_FILE
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
    form.value.files = (res.data.files || []).map(file => ({
      filePath: file.filePath,
      description: file.description,
      content: file.content,
      sort: file.sort,
    }))
    activeFile.value = MAIN_FILE
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
 * 切换当前编辑的文件
 * @param filePath 文件标识
 */
function handleSwitchFile(filePath) {
  activeFile.value = filePath
}

/**
 * 打开添加文件弹窗
 */
function handleOpenFileDialog() {
  fileForm.value.filePath = undefined
  fileForm.value.description = undefined
  fileDialogVisible.value = true
}

/**
 * 确认添加技能文件，加入文件列表并切换为当前编辑文件
 */
function handleConfirmAddFile() {
  fileFormRef.value.validate(valid => {
    if (!valid) return
    const filePath = fileForm.value.filePath.trim()
    if (filePath === MAIN_FILE) {
      ElMessage.warning('SKILL.md 为主文件，请直接在文件列表中编辑')
      return
    }
    if (form.value.files.some(f => f.filePath === filePath)) {
      ElMessage.warning('文件路径已存在')
      return
    }
    form.value.files.push({
      filePath,
      description: fileForm.value.description.trim(),
      content: '',
      sort: form.value.files.length + 1,
    })
    activeFile.value = filePath
    fileDialogVisible.value = false
  })
}

/**
 * 删除技能文件
 * @param filePath 文件路径
 */
function handleRemoveFile(filePath) {
  ElMessageBox.confirm('是否确定删除该文件?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    form.value.files = form.value.files.filter(f => f.filePath !== filePath)
    if (activeFile.value === filePath) {
      activeFile.value = MAIN_FILE
    }
  }).catch(() => {})
}

/**
 * 提交表单（新增 / 修改），技能文件序列化为JSON随表单提交
 */
function handleSubmitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    const emptyFile = form.value.files.find(f => !f.content || !f.content.trim())
    if (emptyFile) {
      ElMessage.warning(`文件 ${emptyFile.filePath} 内容为空`)
      activeFile.value = emptyFile.filePath
      return
    }
    const data = {
      id: form.value.id,
      name: form.value.name,
      description: form.value.description,
      content: form.value.content,
      status: form.value.status,
      files: form.value.files.length ? JSON.stringify(form.value.files) : undefined,
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
  margin-bottom: $spacing-md;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
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
  height: calc(100vh - #{$nav-height} - 175px);
  overflow-y: auto;
  padding-top: 6px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}

.empty-grid {
  height: calc(100vh - #{$nav-height} - 175px);
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

.skill-file-layout {
  display: flex;
  gap: $spacing-md;
  width: 100%;
}

.skill-file-panel {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  overflow-y: auto;
  padding: $spacing-sm;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.skill-file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-xs;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-sm;
  font-size: 13px;
  color: $color-text-primary;
  cursor: pointer;
  transition: $transition-normal;

  &:hover {
    background-color: $color-primary-light;
  }

  &.active {
    background-color: $color-primary-light;
    color: $color-primary;
    font-weight: 600;
  }
}

.skill-file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.skill-file-tag {
  flex-shrink: 0;
  font-size: 12px;
  color: $color-text-placeholder;
}

.skill-file-delete {
  flex-shrink: 0;
  color: $color-text-placeholder;

  &:hover {
    color: $color-primary;
  }
}

.skill-file-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-xs $spacing-sm;
  border: 1px dashed $el-border-color-darker;
  border-radius: $border-radius-sm;
  font-size: 13px;
  color: $color-text-secondary;
  cursor: pointer;
  transition: $transition-normal;

  &:hover {
    color: $color-primary;
    border-color: $color-primary;
  }
}

.skill-file-editor {
  flex: 1;
  min-width: 0;
}
</style>
