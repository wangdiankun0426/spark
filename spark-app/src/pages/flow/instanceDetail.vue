<template>
  <view class="detail-container safe-area-page" :class="themeClass">
    <PageHeader title="流程详情" @back="handleBack"/>

    <!-- 内容区 -->
    <scroll-view class="detail-body" scroll-y>
      <!-- 基本信息：流程标题作为第一行，状态胶囊挂在分组标题右侧 -->
      <view class="section-card">
        <view class="section-head">
          <SectionTitle>基本信息</SectionTitle>
          <text class="tag section-tag" :class="'is-' + statusTone(instance.status)">
            {{ instance.statusName || '处理中' }}
          </text>
        </view>
        <view class="kv-row">
          <text class="kv-key">流程标题</text>
          <text class="kv-value kv-value--strong">{{ instance.name || '-' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">流程类型</text>
          <text class="kv-value">{{ instance.typeName || '-' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">申请人</text>
          <text class="kv-value">{{ instance.createdByName || '-' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">申请部门</text>
          <text class="kv-value">{{ instance.deptName || '-' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">紧急程度</text>
          <text class="kv-value" :class="'lvl-' + (instance.level || 1)">{{ instance.levelName || '-' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">申请时间</text>
          <text class="kv-value">{{ instance.createdDt || '-' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">备注</text>
          <text class="kv-value">{{ instance.description || '无' }}</text>
        </view>
      </view>

      <!-- 表单详情 -->
      <view class="section-card">
        <view class="section-head">
          <SectionTitle>表单详情</SectionTitle>
        </view>
        <template v-if="visibleWidgetList.length">
          <view class="kv-row" v-for="widget in visibleWidgetList" :key="widget.config.code">
            <text class="kv-key">{{ widget.config.label }}</text>
            <form-field-value class="kv-value" :widget="widget"/>
          </view>
        </template>
        <view v-else class="empty-row">
          <text>暂无表单数据</text>
        </view>
      </view>

      <!-- 审批记录：时间线 -->
      <view class="section-card">
        <view class="section-head">
          <SectionTitle>审批记录</SectionTitle>
          <text v-if="nodes.length" class="section-count">{{ nodes.length }} 个节点</text>
        </view>
        <view v-if="nodes.length" class="timeline">
          <view class="tl-item" v-for="(node, index) in nodes" :key="node.id || index">
            <view class="tl-rail">
              <view class="tl-dot" :class="'dot-' + statusTone(node.status)"></view>
              <view v-if="index < nodes.length - 1" class="tl-line"></view>
            </view>
            <view class="tl-body">
              <view class="tl-head">
                <text class="tl-name">{{ node.name }}</text>
                <text class="tag tag--mini" :class="'is-' + statusTone(node.status)">{{ node.statusName }}</text>
              </view>
              <view v-if="node.status === 2 && node.unAssigneeName" class="tl-note">
                待审批人：{{ node.unAssigneeName }}
              </view>
              <view class="tl-discuss" v-for="(discuss, dIndex) in node.discusses" :key="discuss.id || dIndex">
                <view class="tl-discuss-head">
                  <text class="tl-discuss-name">{{ discuss.assigneeName }}</text>
                  <text class="tag tag--mini" :class="'is-' + statusTone(discuss.status)">{{ discuss.statusName }}</text>
                  <text class="tl-discuss-time">{{ discuss.createdDt }}</text>
                </view>
                <text v-if="discuss.discuss" class="tl-discuss-content">{{ discuss.discuss }}</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-row">
          <text>暂无审批记录</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="detail-footer" v-if="showFooter">
      <!-- 次要操作：转办/加签/抄送，横向排开，不抢主按钮的视觉 -->
      <view
          v-if="type === 3 && (hasPermission(16) || hasPermission(32) || hasPermission(64))"
          class="footer-extra"
      >
        <view v-if="hasPermission(16)" class="extra-btn" @click="openUserPicker('transfer')">转办</view>
        <view v-if="hasPermission(32)" class="extra-btn" @click="openUserPicker('addSign')">加签</view>
        <view v-if="hasPermission(64)" class="extra-btn" @click="openUserPicker('copy')">抄送</view>
      </view>

      <view class="footer-actions">
        <!-- 催办：描边次级按钮，先弹确认再发通知，避免误触 -->
        <view v-if="canUrge" class="action-btn action-btn--soft" @click="urgeVisible = true">
          <up-icon name="bell" size="16" :color="headerIconColor"/>
          <text class="action-btn__text">{{ urging ? '催办中...' : '催办审批人' }}</text>
        </view>
        <view
            v-if="type === 3 && hasPermission(2)"
            class="action-btn action-btn--ghost"
            @click="openApprovalDialog(4)"
        >驳回</view>
        <view
            v-if="type === 3 && hasPermission(1)"
            class="action-btn action-btn--primary"
            @click="openApprovalDialog(3)"
        >同意</view>
        <!-- 申请页签：发起人在审批中可撤回 -->
        <view
            v-if="canRecall"
            class="action-btn action-btn--primary"
            @click="handleRecall"
        >撤回申请</view>
      </view>
    </view>

    <!-- 弹层宿主：小程序端 modal/picker 内部的 popup 插槽内容会参与 flex 分配顶高底部操作栏 -->
    <view class="sheet-host">
      <!-- 催办确认弹窗 -->
      <up-modal
          :show="urgeVisible"
          title="催办审批人"
          content="将向当前审批人发送一条催办通知，确定发送吗？"
          showCancelButton
          confirmText="发送催办"
          @confirm="handleUrge"
          @cancel="urgeVisible = false"
      />

      <!-- 审批意见弹窗 -->
      <up-modal
          :show="approvalVisible"
          :title="approvalStatus === 3 ? '通过' : '驳回'"
          showCancelButton
          @confirm="handleApproval"
          @cancel="approvalVisible = false"
      >
        <view class="modal-body">
          <up-textarea v-model="approvalDiscuss" placeholder="请输入审批意见" count :maxlength="200" :customStyle="textareaStyle"/>
        </view>
      </up-modal>

      <!-- 加签、转办、抄送选人弹层，复用选择用户组件 -->
      <user-picker
          v-model:show="userPickerVisible"
          :title="operateType === 'transfer' ? '选择转办人' : operateType === 'copy' ? '选择抄送人' : '选择加签人'"
          :multiple="operateType !== 'transfer'"
          :loading="operating"
          @confirm="handleUserConfirm"
      />
    </view>
  </view>
</template>

<script setup>
import {computed, ref} from "vue";
import {onLoad} from "@dcloudio/uni-app";
import {
  showInstanceDetailAPI,
  approvalFlowInstanceAPI,
  urgeFlowInstanceAPI,
  transferFlowInstanceAPI,
  addSignFlowInstanceAPI,
  recallFlowInstanceAPI,
  copyFlowInstanceAPI
} from "@/api/flow/instance";
import {toast} from "uview-plus";
import UserPicker from '@/components/UserPicker/index.vue'
import FormFieldValue from '@/components/FormFieldValue/index.vue'
import PageHeader from '@/components/PageHeader/index.vue'
import SectionTitle from '@/components/SectionTitle/index.vue'
import {useTheme} from '@/utils/useTheme'
import {getHeaderIconColor, getSearchBgColor} from '@/utils/themeUtil'
import store from "@/store/index.js";
import {useDebounceFn} from "@/utils/debounce";

const {isDark, themeClass} = useTheme();

// 头部返回与描边按钮图标共用品牌色，暗色下提亮一档
const headerIconColor = computed(() => getHeaderIconColor(isDark.value));

const instanceId = ref(undefined);
// 详情类型：1 我的申请 3 我的待办 4 我的已办 5 抄送给我
const type = ref(0);
const instance = ref({});
const widgetList = ref([]);
const nodes = ref([]);
const nodeId = ref(undefined);
const nodePermission = ref(undefined);
const urging = ref(false);
const recalling = ref(false);
// 催办确认弹窗显隐
const urgeVisible = ref(false);

/** 当前登录用户ID */
const currentUserId = computed(() => {
  const userInfo = store.getters['user/getUserInfo'];
  return userInfo ? userInfo.id : undefined;
});

/** 当前用户是否为流程发起人 */
const isCreator = computed(() => {
  return instance.value.createdBy !== undefined && instance.value.createdBy === currentUserId.value;
});

onLoad((options) => {
  instanceId.value = Number(options.id);
  type.value = Number(options.type);
  handleLoadDetail();
});

/**
 * 加载流程实例详情
 */
function handleLoadDetail() {
  showInstanceDetailAPI({id: instanceId.value}).then(res => {
    if (res.code !== 200) {
      return;
    }
    instance.value = res.data;
    // 给模板字段回填 value 与 showValue
    const formJson = res.data.formJson ? JSON.parse(res.data.formJson) : {};
    const values = res.data.values || [];
    const codes = values.map(value => value.code);
    (formJson.widgetList || []).forEach(widget => {
      const index = codes.indexOf(widget.config.code);
      if (index === -1) {
        return;
      }
      widget.config.value = values[index].value;
      widget.config.showValue = values[index].showValue;
    });
    widgetList.value = formJson.widgetList || [];
    nodes.value = res.data.nodes || [];
    nodeId.value = res.data.nodeId;
    nodePermission.value = res.data.permission;
  });
}

/**
 * 过滤隐藏字段的表单组件
 */
const visibleWidgetList = computed(() => {
  return widgetList.value.filter(widget => !widget.config.hidden);
});

/**
 * 是否展示底部操作栏
 */
const showFooter = computed(() => {
  if (type.value === 3) {
    return nodeId.value !== undefined && (hasPermission(1) || hasPermission(2) || hasPermission(16) || hasPermission(32) || hasPermission(64));
  }
  if (type.value === 1) {
    return canUrge.value || canRecall.value;
  }
  return false;
});

// 催办：我的申请 + 审批中 + 有催办权限
const canUrge = computed(() => type.value === 1 && instance.value.status === 2
    && nodeId.value !== undefined && hasPermission(8));
// 撤回：我的申请 + 审批中 + 本人发起 + 有撤回权限
const canRecall = computed(() => type.value === 1 && instance.value.status === 2
    && isCreator.value && hasPermission(128));
/**
 * 判断节点操作权限
 * @param bit 权限位 1-通过 2-驳回 8-催办 16-转办 32-加签 64-抄送 128-撤回
 */
function hasPermission(bit) {
  return ((nodePermission.value || 0) & bit) === bit;
}

/**
 * 返回列表页
 */
function handleBack() {
  uni.navigateBack();
}

const approvalVisible = ref(false);
const approvalStatus = ref(3);
const approvalDiscuss = ref('');

// 小程序原生输入组件拿不到 CSS 变量，customStyle 按皮肤给实色
const textareaStyle = computed(() => ({
  backgroundColor: getSearchBgColor(isDark.value),
  borderRadius: '8rpx'
}));

/**
 * 打开审批意见弹窗
 * @param status 审批状态 3-通过 4-驳回
 */
const openApprovalDialog = useDebounceFn((status) => {
  approvalStatus.value = status;
  approvalDiscuss.value = '';
  approvalVisible.value = true;
});

/**
 * 确认审批
 */
const handleApproval = useDebounceFn(() => {
  if (!approvalDiscuss.value || !approvalDiscuss.value.trim()) {
    toast('审批意见不能为空');
    return;
  }
  approvalFlowInstanceAPI({
    id: instanceId.value,
    nodeId: nodeId.value,
    status: approvalStatus.value,
    discuss: approvalDiscuss.value
  }).then(res => {
    if (res.code === 200) {
      toast('审批成功');
      approvalVisible.value = false;
      setTimeout(handleBack, 500);
    }
  });
});

/**
 * 催办流程实例：由确认弹窗触发，避免点一下就发通知
 */
const handleUrge = useDebounceFn(() => {
  if (urging.value) {
    return;
  }
  urgeVisible.value = false;
  urging.value = true;
  urgeFlowInstanceAPI({id: instanceId.value}).then(res => {
    if (res.code === 200) {
      toast('催办通知已发送给当前审批人');
    }
  }).finally(() => {
    urging.value = false;
  });
});

/**
 * 撤回流程实例
 */
const handleRecall = useDebounceFn(() => {
  uni.showModal({
    title: '提示',
    content: '确定撤回该流程实例吗？撤回后流程将终止。',
    success: (res) => {
      if (!res.confirm) return;
      recalling.value = true;
      recallFlowInstanceAPI({id: instanceId.value}).then(res => {
        if (res.code === 200) {
          toast('撤回成功');
          setTimeout(handleBack, 500);
        }
      }).finally(() => {
        recalling.value = false;
      });
    }
  });
});

const userPickerVisible = ref(false);
const operateType = ref('transfer');
const operating = ref(false);

/**
 * 打开加签/转办选人弹层
 * @param type transfer-转办 addSign-加签
 */
const openUserPicker = useDebounceFn((type) => {
  operateType.value = type;
  userPickerVisible.value = true;
});

/**
 * 确认加签/转办/抄送
 * @param users 选中的用户列表
 */
const handleUserConfirm = useDebounceFn((users) => {
  const userIds = users.map(user => user.id).join(',');
  operating.value = true;
  if (operateType.value === 'copy') {
    copyFlowInstanceAPI({
      instanceId: instanceId.value,
      nodeId: nodeId.value,
      userIds: userIds
    }).then(res => {
      if (res.code === 200) {
        toast('抄送成功');
        userPickerVisible.value = false;
      }
    }).finally(() => {
      operating.value = false;
    });
  } else {
    const api = operateType.value === 'transfer' ? transferFlowInstanceAPI : addSignFlowInstanceAPI;
    api({
      id: instanceId.value,
      nodeId: nodeId.value,
      assigneeIds: userIds
    }).then(res => {
      if (res.code === 200) {
        toast(operateType.value === 'transfer' ? '转办成功' : '加签成功');
        userPickerVisible.value = false;
        setTimeout(handleBack, 500);
      }
    }).finally(() => {
      operating.value = false;
    });
  }
});

/**
 * 状态 → 颜色语义，胶囊与时间线圆点共用同一套映射
 * @param status 流程/节点状态值
 * @returns {string} 颜色语义类名后缀
 */
function statusTone(status) {
  if (status === 3 || status === 5) {
    return 'success';
  }
  if (status === 4) {
    return 'danger';
  }
  if (status === 1) {
    return 'warning';
  }
  if (status === 2 || status === 6) {
    return 'info';
  }
  return 'neutral';
}
</script>

<style scoped lang="scss">
.detail-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: $app-bg-page;
}


.detail-body {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 12px 14px 16px;
  box-sizing: border-box;
}

.tag {
  flex-shrink: 0;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 600;
  line-height: 1.3;
  white-space: nowrap;
}

.tag--mini {
  padding: 1px 7px;
  font-size: 10px;
}

.tag.is-neutral {
  color: $app-text-secondary;
  background-color: $app-bg-sub;
}

.tag.is-info {
  color: $app-text-info;
  background-color: $app-bg-info;
}

.tag.is-warning {
  color: $app-text-warning;
  background-color: $app-bg-warning;
}

.tag.is-success {
  color: $app-text-success;
  background-color: $app-bg-success;
}

.tag.is-danger {
  color: $app-text-error;
  background-color: $app-bg-error;
}

.section-tag {
  margin-left: auto;
}

.section-card {
  padding: 13px 14px 4px;
  margin-bottom: 10px;
  border-radius: 16px;
  background-color: $app-bg-card;
  box-shadow: $app-shadow-card;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-bottom: 8px;
}

.section-count {
  margin-left: auto;
  font-size: 11px;
  color: $app-text-tertiary;
}

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

.kv-value--strong {
  font-size: 13.5px;
  font-weight: 700;
}

.kv-value.lvl-2 {
  color: $app-text-warning;
  font-weight: 600;
}

.kv-value.lvl-3 {
  color: $app-text-error;
  font-weight: 600;
}

.empty-row {
  display: flex;
  justify-content: center;
  padding: 14px 0 16px;
  font-size: 12px;
  color: $app-text-tertiary;
}

.timeline {
  padding-top: 2px;
}

.tl-item {
  display: flex;
  gap: 10px;
}

.tl-rail {
  flex-shrink: 0;
  width: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tl-dot {
  width: 8px;
  height: 8px;
  margin-top: 5px;
  border-radius: 50%;
  background-color: $app-border;
  box-shadow: 0 0 0 3px $app-bg-card;
}

.tl-dot.dot-info {
  background-color: $app-text-info;
}

.tl-dot.dot-warning {
  background-color: $app-text-warning;
}

.tl-dot.dot-success {
  background-color: $app-text-success;
}

.tl-dot.dot-danger {
  background-color: $app-text-error;
}

.tl-line {
  flex: 1;
  width: 2px;
  margin: 3px 0;
  border-radius: 2px;
  background-color: $app-border;
}

.tl-body {
  flex: 1;
  min-width: 0;
  padding-bottom: 12px;
}

.tl-head {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 18px;
}

.tl-name {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  font-weight: 600;
  color: $app-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tl-note {
  margin-top: 4px;
  font-size: 11.5px;
  color: $app-text-tertiary;
}

.tl-discuss {
  margin-top: 7px;
  padding: 8px 10px;
  border-radius: 10px;
  background-color: $app-bg-sub;
}

.tl-discuss-head {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 11.5px;
}

.tl-discuss-name {
  color: $app-text-regular;
  font-weight: 500;
}

.tl-discuss-time {
  margin-left: auto;
  font-size: 10.5px;
  color: $app-text-tertiary;
}

.tl-discuss-content {
  display: block;
  margin-top: 5px;
  font-size: 11.5px;
  line-height: 1.6;
  color: $app-text-regular;
  word-break: break-all;
}

.detail-footer {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 14px;
  padding-bottom: calc(10px + constant(safe-area-inset-bottom));
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background-color: $app-bg-card;
  border-top: 1px solid $app-border-light;
}

.footer-extra {
  display: flex;
  align-items: center;
  gap: 8px;
}

.extra-btn {
  height: 30px;
  padding: 0 13px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: $app-text-secondary;
  background-color: $app-bg-sub;
  border: 1px solid $app-border-light;

  &:active {
    background-color: $app-bg-hover;
  }
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

.action-btn--soft {
  gap: 6px;
  color: $app-brand;
  background-color: $app-bg-card;
  border: 1px solid $app-brand-border;

  &:active {
    background-color: $app-primary-soft;
  }
}

.action-btn__text {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.action-btn--ghost {
  color: $app-text-error;
  background-color: $app-bg-card;
  border: 1px solid $app-error-border;
}

.action-btn--ghost:active {
  background-color: $app-bg-error;
}

.modal-body {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 0;
}
</style>
