<template>
  <view class="wf-instance-detail safe-area-page" :class="themeClass">
    <PageHeader title="运行详情" @back="goBack"/>

    <!-- 分类页签：与 PC 端详情弹窗同口径分两页 -->
    <view class="detail-tabs">
      <view class="tabs-track">
        <view
            v-for="(tab, index) in detailTabs"
            :key="index"
            class="seg-tab"
            :class="{ 'is-active': index === detailTab }"
            @click="detailTab = index"
        >
          <text class="seg-tab__text">{{ tab }}</text>
        </view>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view class="detail-content" scroll-y>
      <!-- 加载中 -->
      <view v-if="loading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <view v-else-if="instance">
      <template v-if="detailTab === 0">
      <!-- 实例数据 -->
      <view class="detail-section">
        <view class="section-head">
          <SectionTitle>实例数据</SectionTitle>
        </view>
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

      <!-- 表单信息：与流程详情表单信息同款行式布局 -->
      <view class="detail-section" v-if="formFields.length > 0">
        <view class="section-head">
          <SectionTitle>表单信息</SectionTitle>
        </view>
        <view class="kv-row" v-for="(item, index) in formFields" :key="index">
          <text class="kv-key">{{ item.label }}</text>
          <view class="kv-value">
            <form-field-value :type="item.type" :value="item.value" :showValue="item.showValue"/>
          </view>
        </view>
      </view>
      <view class="detail-section" v-else-if="formLoaded">
        <view class="section-head">
          <SectionTitle>表单信息</SectionTitle>
        </view>
        <view class="empty-text">
          <text>无表单数据</text>
        </view>
      </view>

      </template>

      <template v-else>
      <!-- 错误信息 -->
      <view class="detail-section error-section" v-if="instance.errorMsg">
        <view class="section-head">
          <SectionTitle danger>错误信息</SectionTitle>
        </view>
        <view class="error-content">
          <text class="error-text">{{ instance.errorMsg }}</text>
        </view>
      </view>

      <!-- 执行日志 -->
      <view class="detail-section">
        <view class="section-head">
          <SectionTitle>执行日志</SectionTitle>
        </view>
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
                  :color="nodeArrowColor"
              ></up-icon>
            </view>

            <!-- 节点入参出参 -->
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
      </template>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-box">
        <up-empty text="未找到实例详情" icon="list"/>
      </view>
    </scroll-view>

    <!-- 底部审核操作栏：实例挂起且当前人在审核人列表时显示 -->
    <view class="detail-footer" v-if="canReview">
      <view class="footer-actions">
        <view class="action-btn action-btn--ghost" @click="openReviewDialog(false)">驳回</view>
        <view class="action-btn action-btn--primary" @click="openReviewDialog(true)">通过</view>
      </view>
    </view>

    <!-- 弹层宿主：小程序端 modal 内部的 popup 插槽内容会参与 flex 分配顶高底部操作栏 -->
    <view class="sheet-host">
      <up-modal
          :show="reviewVisible"
          :title="reviewApproved ? '通过审核' : '驳回审核'"
          showCancelButton
          confirmText="确定"
          @confirm="submitReview"
          @cancel="reviewVisible = false"
      >
        <view class="modal-body">
          <up-textarea v-model="reviewComment" placeholder="请输入审核意见，选填" count :maxlength="200" :customStyle="textareaStyle"/>
        </view>
      </up-modal>
    </view>
  </view>
</template>

<script setup>
import {ref, computed} from 'vue'
import {onLoad} from '@dcloudio/uni-app'
import {useTheme} from '@/utils/useTheme'
import {getSearchBgColor, getTertiaryColor} from '@/utils/themeUtil'
import {queryInstanceDetailAPI, queryInstanceNodesAPI, reviewWorkflowInstanceAPI} from '@/api/workflow/instance'
import {detailFormValueAPI} from '@/api/form/formValue'
import {toast} from 'uview-plus'
import FormFieldValue from '@/components/FormFieldValue/index.vue'
import PageHeader from '@/components/PageHeader/index.vue'
import SectionTitle from '@/components/SectionTitle/index.vue'
import store from '@/store/index.js'
import {useDebounceFn} from '@/utils/debounce'

const {isDark, themeClass} = useTheme();

// 节点展开箭头用次级图标色，暗色下提亮保证对比度
const nodeArrowColor = computed(() => getTertiaryColor(isDark.value));
const instanceId = ref(null)
const loading = ref(false)
const nodesLoading = ref(false)
const instance = ref(null)
const nodes = ref([])
const formFields = ref([])
const formLoaded = ref(false)

const reviewVisible = ref(false)
const reviewApproved = ref(true)
const reviewComment = ref('')
const reviewSubmitting = ref(false)

// 页签：实例详情与执行日志两页
const detailTabs = ['实例详情', '执行日志']
const detailTab = ref(0)

// 小程序原生输入组件拿不到 CSS 变量，customStyle 按皮肤给实色
const textareaStyle = computed(() => ({
  backgroundColor: getSearchBgColor(isDark.value),
  borderRadius: '8rpx'
}));

// 最新一条挂起中的人工审核节点行
const waitingReviewNode = computed(() => {
  const rows = nodes.value.filter(n => n.status === 1 && n.nodeType === 'humanReview')
  return rows.length > 0 ? rows[rows.length - 1] : null
});

// 审核人未配置视为不限制，与后端待审列表规则一致
const canReview = computed(() => {
  if (!instance.value || instance.value.status !== 6 || !waitingReviewNode.value) {
    return false
  }
  const userInfo = store.getters['user/getUserInfo']
  const userId = userInfo ? userInfo.id : undefined
  let reviewerIds = ''
  try {
    const info = JSON.parse(waitingReviewNode.value.outputJson || '{}')
    reviewerIds = info.reviewerIds || ''
  } catch (e) {
    reviewerIds = ''
  }
  if (!reviewerIds) {
    return true
  }
  return String(reviewerIds).split(',').includes(String(userId))
});

/**
 * 打开审核意见弹窗
 * @param approved 审核结果，true通过 false驳回
 */
const openReviewDialog = useDebounceFn((approved) => {
  reviewApproved.value = approved
  reviewComment.value = ''
  reviewVisible.value = true
});

/**
 * 提交审核结论
 */
const submitReview = useDebounceFn(() => {
  if (reviewSubmitting.value) {
    return
  }
  reviewSubmitting.value = true
  const comment = reviewComment.value.trim()
  reviewWorkflowInstanceAPI({
    instanceId: instanceId.value,
    approved: reviewApproved.value,
    comment: comment
  }).then(res => {
    if (res.code === 200) {
      toast(reviewApproved.value ? '已通过，流程继续执行' : '已驳回')
      reviewVisible.value = false
      loadData(instanceId.value)
    }
  }).finally(() => {
    reviewSubmitting.value = false
  })
});

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
        _expanded: true
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
            fields.push({
              // 字段类型决定展示形式，上传附件需解析 JSON 后展示文件名称
              type: widget && widget.type ? widget.type : '',
              label: widget && widget.config && widget.config.label ? widget.config.label : v.code,
              value: v.value,
              showValue: v.showValue
            })
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
  background-color: $app-bg-page;
}

.detail-content {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 10px 12px;
  box-sizing: border-box;
}

.detail-tabs {
  flex-shrink: 0;
  padding: 0 14px 12px;
  background-color: $app-bg-card;
}

.tabs-track {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 38px;
  padding: 3px;
  border-radius: 12px;
  background-color: $app-tab-track;
}

.seg-tab {
  flex: 1;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  transition: background 0.25s ease, box-shadow 0.25s ease;
}

.seg-tab__text {
  font-size: 12.5px;
  color: $app-text-secondary;
  white-space: nowrap;
  transition: color 0.25s ease;
}

.seg-tab.is-active {
  background-image: $app-gradient-fill;
  box-shadow: $app-shadow-brand;
}

.seg-tab.is-active .seg-tab__text {
  color: $app-fill-tx;
  font-weight: 600;
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
  color: $app-text-secondary;
}

.detail-section {
  margin-bottom: 14px;
  padding: 14px;
  background-color: $app-bg-card;
  border-radius: 16px;
  box-shadow: $app-shadow-card;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-head {
  margin-bottom: 12px;
}

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
  color: $app-text-secondary;
  flex-shrink: 0;
  width: 80px;
}

.detail-value {
  font-size: 13px;
  color: $app-text-primary;
  text-align: right;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-text {
  font-weight: 500;

  &.status-1 {
    color: $app-text-info;
  }
  &.status-2 {
    color: $app-text-success;
  }
  &.status-3 {
    color: $app-text-warning;
  }
  &.status-4 {
    color: $app-text-error;
  }
  &.status-5 {
    color: $app-text-tertiary;
  }
  &.status-6 {
    color: $app-text-warning;
  }
  &.status-7 {
    color: $app-text-error;
  }
}

.detail-footer {
  flex-shrink: 0;
  padding: 10px 14px;
  padding-bottom: calc(10px + constant(safe-area-inset-bottom));
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background-color: $app-bg-card;
  border-top: 1px solid $app-border-light;
}

.footer-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14.5px;
  font-weight: 600;
  letter-spacing: 1px;

  &:active {
    opacity: 0.85;
  }
}

.action-btn--primary {
  color: $app-fill-tx;
  background-image: $app-gradient-fill;
  box-shadow: $app-shadow-brand;
}

.action-btn--ghost {
  color: $app-text-error;
  background-color: $app-bg-card;
  border: 1px solid $app-error-border;
}

.modal-body {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 0;
}

.error-content {
  background-color: $app-bg-error;
  border: 1px solid $app-border;
  border-radius: 8px;
  padding: 12px;
}

.error-text {
  font-size: 13px;
  color: $app-text-error;
  word-break: break-all;
}

// 表单信息行：与 flow/instanceDetail 的 kv 配方保持一致
.kv-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 8px 0;
  border-top: 1px solid $app-divide;
  font-size: 12.5px;
}

.kv-key {
  flex-shrink: 0;
  width: 68px;
  line-height: 18px;
  color: $app-text-tertiary;
}

.kv-value {
  flex: 1;
  min-width: 0;
  text-align: right;
  line-height: 18px;
  color: $app-text-primary;
  word-break: break-all;
}

.nodes-loading, .nodes-empty {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.nodes-empty text {
  font-size: 13px;
  color: $app-text-secondary;
}

.nodes-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-item {
  background-color: $app-bg-card;
  border: 1px solid $app-border-light;
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
    background-color: $app-text-info;
  }
  &.node-status-2 {
    background-color: $app-text-success;
  }
  &.node-status-3 {
    background-color: $app-text-warning;
  }
  &.node-status-4 {
    background-color: $app-text-error;
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
  color: $app-text-primary;
  font-weight: 500;
}

.node-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.node-type {
  font-size: 11px;
  color: $app-text-purple;
  background-color: $app-bg-purple;
  padding: 1px 6px;
  border-radius: 4px;
}

.node-status-text {
  font-size: 12px;
  color: $app-text-regular;
}

.node-params {
  padding: 0 12px 12px;
  border-top: 1px solid $app-border-light;
}

.param-block {
  margin-top: 10px;
}

.param-title {
  font-size: 12px;
  font-weight: 600;
  color: $app-text-regular;
  margin-bottom: 6px;
  display: block;
}

.param-content {
  background-color: $app-bg-card;
  border-radius: 6px;
  padding: 8px;
  border: 1px solid $app-border-light;
  max-height: 160px;
  overflow-y: auto;
}

.param-json {
  font-size: 12px;
  color: $app-text-primary;
  font-family: monospace;
  word-break: break-all;
  white-space: pre-wrap;
  display: block;
  line-height: 1.6;
}
</style>
