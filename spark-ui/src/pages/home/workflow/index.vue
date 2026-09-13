<template>
  <div class="app-container wf-container">
    <!-- 主体：左侧工作流列表 + 右侧运行区 -->
    <div class="wf-body">
      <!-- 左侧可用工作流 -->
      <div class="session-pane">
        <div class="session-pane-title-row">
          <div class="session-pane-title">工作流</div>
          <span class="session-pane-count">{{ workflowList.length }} 个</span>
        </div>
        <el-scrollbar v-if="workflowList.length" class="session-list" always>
          <div
              v-for="item in workflowList"
              :key="item.id"
              class="session-item"
              :class="{ 'session-item-active': currentWorkflow.id === item.id }"
              @click="handleSelect(item)"
          >
            <div class="session-item-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="session-item-info">
              <div class="session-item-title">{{ item.name }}</div>
              <div class="session-item-meta">
                <span v-if="item.revNum">v{{ item.revNum }}</span>
                <span v-if="item.description">{{ item.description }}</span>
              </div>
            </div>
          </div>
        </el-scrollbar>
        <el-empty v-else class="session-empty" description="暂无可用工作流" :image-size="90" />
      </div>

      <!-- 右侧：输入表单 + 开始运行 -->
      <div class="chat-pane">
        <template v-if="currentWorkflow.id">
          <div class="wf-pane-header">
            <el-icon class="wf-pane-icon"><Connection /></el-icon>
            <span class="wf-pane-title">{{ currentWorkflow.name }}</span>
            <span v-if="currentWorkflow.revNum" class="wf-pane-version">v{{ currentWorkflow.revNum }}</span>
          </div>
          <el-scrollbar class="wf-form-body" always>
            <form-view
                v-if="formJson && formJson.widgetList && formJson.widgetList.length"
                :form="formJson"
            />
            <el-empty v-else description="此工作流无输入参数" :image-size="60" />
          </el-scrollbar>
          <div class="wf-footer">
            <el-button
                type="primary"
                :loading="running"
                :disabled="!formJson"
                class="wf-run-btn"
                @click="handleRun"
            >
              <el-icon><VideoPlay /></el-icon>{{ running ? '运行中...' : '开始运行' }}
            </el-button>
          </div>
        </template>
        <el-empty v-else class="wf-empty" description="请选择工作流" :image-size="90" />
      </div>
    </div>

    <!-- 运行结果抽屉，从左侧滑出 -->
    <wf-instance-detail-drawer
        v-model="resultVisible"
        :instance-id="resultInstanceId"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection, VideoPlay } from '@element-plus/icons-vue'
import { pageWorkflowListAPI, queryWorkflowDetailAPI } from '@/api/workflow/template.js'
import { runWorkflowAPI, queryInstanceDetailAPI } from '@/api/workflow/instance.js'
import FormView from '@/components/FormView'
import WfInstanceDetailDrawer from '@/components/WfInstanceDetailDrawer/index.vue'

// 运行结果轮询次数，每次间隔 1 秒
const RUN_POLL_RETRIES = 30

const workflowList = ref([])
const currentWorkflow = ref({})
const formJson = ref(null)
const formId = ref(null)
const running = ref(false)
const resultVisible = ref(false)
const resultInstanceId = ref(null)

onMounted(() => {
  loadWorkflows()
})

/**
 * 加载可用工作流，默认选中第一个
 */
function loadWorkflows() {
  const query = { status: 1, page: false }
  pageWorkflowListAPI(query).then(res => {
    if (res.code !== 200) {
      return
    }
    workflowList.value = res.data.rows || []
    if (!workflowList.value.length) {
      ElMessage.warning('暂无可用工作流')
      return
    }
    handleSelect(workflowList.value[0])
  })
}

/**
 * 选择工作流，加载其绑定的输入表单
 */
function handleSelect(item) {
  currentWorkflow.value = item
  formJson.value = null
  formId.value = null
  loadFormJson(item.id)
}

/**
 * 加载工作流绑定的输入表单定义
 */
function loadFormJson(id) {
  queryWorkflowDetailAPI({ id }).then(res => {
    if (res.code !== 200 || !res.data || !res.data.formJson) {
      return
    }
    formId.value = res.data.formId
    try {
      formJson.value = JSON.parse(res.data.formJson)
    } catch (e) {
      formJson.value = null
    }
  })
}

/**
 * 开始运行：校验必填、收集表单值、等待执行结束后打开结果抽屉
 */
async function handleRun() {
  if (!currentWorkflow.value.id) {
    return
  }
  const widgetList = (formJson.value && formJson.value.widgetList) || []
  const missingWidget = widgetList.find(widget => {
    if (!widget.config || !widget.config.required) {
      return false
    }
    const value = widget.config.value
    return value === null || value === undefined || value === '' || (Array.isArray(value) && value.length === 0)
  })
  if (missingWidget) {
    ElMessage.warning(`【${missingWidget.config.label}】为必填项，请填写后再运行`)
    return
  }
  const values = []
  widgetList.forEach(widget => {
    if (widget.config && widget.config.code) {
      values.push({
        code: widget.config.code,
        type: widget.type,
        value: widget.config.value,
        showValue: widget.config.showValue != null ? widget.config.showValue : widget.config.value
      })
    }
  })
  running.value = true
  try {
    const res = await runWorkflowAPI({
      templateId: currentWorkflow.value.id,
      formId: formId.value,
      values
    })
    if (res.code !== 200 || !res.data || !res.data.id) {
      return
    }
    const instanceId = res.data.id
    await waitInstanceFinished(instanceId)
    resultInstanceId.value = instanceId
    resultVisible.value = true
  } finally {
    running.value = false
  }
}

/**
 * 轮询等待实例运行结束，超时后仍返回，由结果抽屉展示当前状态
 * @param instanceId 运行实例id
 */
async function waitInstanceFinished(instanceId) {
  let retries = RUN_POLL_RETRIES
  while (retries > 0) {
    await new Promise(resolve => setTimeout(resolve, 1000))
    const res = await queryInstanceDetailAPI({ id: instanceId })
    if (res.code === 200 && res.data && res.data.status !== 1) {
      return
    }
    retries--
  }
}
</script>

<style scoped lang="scss">
.wf-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 70px);
}

.wf-body {
  display: flex;
  align-items: stretch;
  gap: $spacing-lg;
  flex: 1;
  min-height: 0;
}

.session-pane {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: $spacing-md;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.session-pane-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  flex-shrink: 0;
  padding: $spacing-xs $spacing-sm $spacing-sm;
}

.session-pane-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-secondary;
}

.session-pane-count {
  font-size: 11px;
  color: $color-text-placeholder;
}

.session-list {
  flex: 1;
  min-height: 0;
}

.session-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border: 1px solid transparent;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: $transition-fast;
  margin-right: 10px;

  &:hover {
    background-color: $color-primary-soft;
  }

  &.session-item-active {
    background-color: $color-primary-light;
    border-color: $color-primary;
  }
}

.session-item-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: $color-text-white;
  background-color: $color-primary;
  border-radius: $border-radius-md;
}

.session-item-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.session-item-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item-meta {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: 11px;
  color: $color-text-placeholder;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.session-empty {
  flex: 1;
}

.chat-pane {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: $spacing-md;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.wf-pane-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  flex-shrink: 0;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-color-light;
}

.wf-pane-icon {
  font-size: 20px;
  color: $color-primary;
}

.wf-pane-title {
  font-size: 16px;
  font-weight: 600;
  color: $color-text-primary;
}

.wf-pane-version {
  font-size: 12px;
  color: $color-text-placeholder;
}

.wf-form-body {
  flex: 1;
  min-height: 0;
  padding: $spacing-md 0;
}

.wf-footer {
  flex-shrink: 0;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-color-light;
}

.wf-run-btn {
  width: 100%;
  height: 40px;
}

.wf-empty {
  flex: 1;
}
</style>
