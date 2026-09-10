<template>
  <view class="wf-instance-detail safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="detail-header">
      <view class="header-left" @click="goBack">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
        <text class="header-title">运行详情</text>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view class="detail-content" scroll-y>
      <!-- 加载中 -->
      <view v-if="loading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <view v-else-if="instance">
      <!-- 表单数据 -->
      <view class="detail-section" v-if="formFields.length > 0">
        <text class="section-title">表单数据</text>
        <view class="form-data-list">
          <view class="form-data-item" v-for="(item, index) in formFields" :key="index">
            <text class="form-label">{{ item.label }}</text>
            <text class="form-value">{{ item.value || '-' }}</text>
          </view>
        </view>
      </view>
      <view class="detail-section" v-else-if="formLoaded">
        <text class="section-title">表单数据</text>
        <view class="empty-text">
          <text>无表单数据</text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="detail-section">
        <text class="section-title">实例数据</text>
        <view class="detail-grid">
          <view class="detail-item">
            <text class="detail-label">工作流</text>
            <text class="detail-value">{{ instance.templateName || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">版本</text>
            <text class="detail-value">{{ instance.revNum || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">状态</text>
            <text class="detail-value status-text" :class="'status-' + instance.status">{{ instance.statusName }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">耗时</text>
            <text class="detail-value">{{ instance.durationMs != null ? instance.durationMs + 'ms' : '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">触发人</text>
            <text class="detail-value">{{ instance.createdByName || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">触发时间</text>
            <text class="detail-value">{{ instance.createdDt || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 错误信息 -->
      <view class="detail-section error-section" v-if="instance.errorMsg">
        <text class="section-title error-title">错误信息</text>
        <view class="error-content">
          <text class="error-text">{{ instance.errorMsg }}</text>
        </view>
      </view>

      <!-- 执行日志 -->
      <view class="detail-section">
        <text class="section-title">执行日志</text>
        <view v-if="nodesLoading" class="nodes-loading">
          <up-loading-icon text="加载节点..."/>
        </view>
        <view v-else-if="nodes.length === 0" class="nodes-empty">
          <text>无节点记录</text>
        </view>
        <view v-else class="nodes-list">
          <view class="node-item" v-for="(node, index) in nodes" :key="index">
            <view class="node-header" @click="toggleNodeExpand(node)">
              <view class="node-left">
                <view class="node-dot" :class="'node-status-' + node.status"></view>
                <view class="node-info">
                  <text class="node-name">{{ node.nodeName || '节点 ' + (index + 1) }}</text>
                  <view class="node-meta">
                    <text class="node-type">{{ node.nodeType || '' }}</text>
                    <text class="node-status-text">{{ node.statusName }}</text>
                  </view>
                </view>
              </view>
              <up-icon
                  :name="node._expanded ? 'arrow-up' : 'arrow-down'"
                  size="14"
                  color="#999"
              ></up-icon>
            </view>

            <!-- 节点入参出参（展开显示） -->
            <view class="node-params" v-if="node._expanded">
              <!-- 入参 -->
              <view class="param-block">
                <text class="param-title">输入数据</text>
                <view class="param-content">
                  <text class="param-json">{{ getNodeText(node.inputJson) || '无' }}</text>
                </view>
              </view>
              <!-- 出参 -->
              <view class="param-block">
                <text class="param-title">输出数据</text>
                <view class="param-content">
                  <text class="param-json">{{ getNodeText(node.outputJson) || '无' }}</text>
                </view>
              </view>
              <!-- 错误信息 -->
              <view class="param-block error-block" v-if="node.errorMsg">
                <text class="param-title">错误信息</text>
                <view class="param-content">
                  <text class="param-json error-text">{{ node.errorMsg }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-box">
        <up-empty text="未找到实例详情" icon="list"/>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import {ref} from 'vue'
import {onLoad} from '@dcloudio/uni-app'
import {queryInstanceDetailAPI, queryInstanceNodesAPI} from '@/api/workflow/instance'
import {detailFormValueAPI} from '@/api/form/formValue'

const instanceId = ref(null)
const loading = ref(false)
const nodesLoading = ref(false)
const instance = ref(null)
const nodes = ref([])
const formFields = ref([])
const formLoaded = ref(false)

onLoad((options) => {
  if (options && options.id) {
    instanceId.value = options.id
    loadData(options.id)
  }
})

/**
 * 加载数据
 */
function loadData(id) {
  loading.value = true
  instance.value = null
  nodes.value = []
  formFields.value = []
  formLoaded.value = false

  // 加载实例详情
  queryInstanceDetailAPI({id}).then(res => {
    if (res.code === 200 && res.data) {
      instance.value = res.data
    }
  }).finally(() => {
    loading.value = false
  })

  // 加载节点列表
  nodesLoading.value = true
  queryInstanceNodesAPI({instanceId: id}).then(res => {
    if (res.code === 200 && res.data) {
      nodes.value = (res.data || []).map(node => ({
        ...node,
        _expanded: false
      }))
    }
  }).finally(() => {
    nodesLoading.value = false
  })

  // 加载表单数据
  loadFormData(id)
}

/**
 * 加载表单数据
 */
function loadFormData(instanceId) {
  formLoaded.value = false
  detailFormValueAPI({objId: instanceId}).then(res => {
    if (res.code === 200 && res.data) {
      const formJson = res.data.formJson
      const values = res.data.values || []
      if (formJson && values.length > 0) {
        try {
          const formConfig = typeof formJson === 'string' ? JSON.parse(formJson) : formJson
          const widgetList = formConfig.widgetList || []
          const fields = []
          values.forEach(v => {
            const widget = widgetList.find(w => w.config && w.config.code === v.code)
            if (widget) {
              fields.push({
                label: widget.config.label || v.code,
                value: v.showValue || v.value || ''
              })
            } else {
              fields.push({
                label: v.code,
                value: v.showValue || v.value || ''
              })
            }
          })
          formFields.value = fields
        } catch (e) {
          formFields.value = []
        }
      }
    }
  }).finally(() => {
    formLoaded.value = true
  })
}

/**
 * 解析节点参数并返回可展示文本
 */
function getNodeText(jsonStr) {
  if (!jsonStr) return ''
  try {
    const out = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    if (typeof out.text === 'string' && out.text) {
      return out.text
    }
    const str = JSON.stringify(out, null, 2)
    return str === '{}' ? '' : str
  } catch (e) {
    return String(jsonStr)
  }
}

/**
 * 展开/收起节点
 */
function toggleNodeExpand(node) {
  node._expanded = !node._expanded
}

/**
 * 返回上一页
 */
function goBack() {
  uni.navigateBack()
}
</script>

<style scoped lang="scss">
.wf-instance-detail {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.detail-header {
  padding: 10px 16px;
  background-color: #0052cc;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.detail-content {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 10px 12px;
  box-sizing: border-box;
}

.loading-box, .empty-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.empty-text {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.empty-text text {
  font-size: 13px;
  color: #999;
}

.detail-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  display: block;
}

/* 基本信息网格 */
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
  width: 80px;
}

.detail-value {
  font-size: 13px;
  color: #333;
  text-align: right;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-text {
  font-weight: 500;

  &.status-1 {
    color: #0052cc;
  }
  &.status-2 {
    color: #52c41a;
  }
  &.status-3 {
    color: #faad14;
  }
  &.status-4 {
    color: #ff4d4f;
  }
}

/* 错误信息 */
.error-section {
  .error-title {
    color: #ff4d4f;
    border-bottom-color: #ffccc7;
  }
}

.error-content {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  padding: 12px;
}

.error-text {
  font-size: 13px;
  color: #ff4d4f;
  word-break: break-all;
}

/* 表单数据 */
.form-data-list {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.form-data-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.form-label {
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
  width: 100px;
}

.form-value {
  font-size: 13px;
  color: #333;
  text-align: right;
  flex: 1;
  word-break: break-all;
}

/* 节点列表 */
.nodes-loading, .nodes-empty {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.nodes-empty text {
  font-size: 13px;
  color: #999;
}

.nodes-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-item {
  background-color: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
}

.node-left {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.node-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;

  &.node-status-1 {
    background-color: #0052cc;
  }
  &.node-status-2 {
    background-color: #52c41a;
  }
  &.node-status-3 {
    background-color: #faad14;
  }
  &.node-status-4 {
    background-color: #ff4d4f;
  }
}

.node-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.node-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.node-type {
  font-size: 11px;
  color: #722ed1;
  background-color: #f9f0ff;
  padding: 1px 6px;
  border-radius: 4px;
}

.node-status-text {
  font-size: 12px;
  color: #666;
}

/* 节点参数展开 */
.node-params {
  padding: 0 12px 12px;
  border-top: 1px solid #eee;
}

.param-block {
  margin-top: 10px;
}

.param-title {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  margin-bottom: 6px;
  display: block;
}

.param-content {
  background-color: #fff;
  border-radius: 6px;
  padding: 8px;
  border: 1px solid #eee;
  max-height: 160px;
  overflow-y: auto;
}

.param-json {
  font-size: 12px;
  color: #333;
  font-family: monospace;
  word-break: break-all;
  white-space: pre-wrap;
  display: block;
  line-height: 1.6;
}
</style>
