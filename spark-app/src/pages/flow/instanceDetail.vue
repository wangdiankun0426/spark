<template>
  <view class="detail-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="detail-header">
      <up-icon name="arrow-left" size="20" color="#ffffff" @click="handleBack"/>
      <text class="header-title">流程详情</text>
    </view>

    <!-- 内容区 -->
    <scroll-view class="detail-body" scroll-y>
      <!-- 基本信息区 -->
      <view class="section-card">
        <view class="section-title">基本信息</view>
        <view class="info-row">
          <text class="info-label">标题</text>
          <text class="info-value">{{ instance.name || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">申请人</text>
          <text class="info-value">{{ instance.createdByName || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">申请部门</text>
          <text class="info-value">{{ instance.deptName || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">紧急程度</text>
          <text class="info-value">{{ instance.levelName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">备注</text>
          <text class="info-value">{{ instance.description || '-' }}</text>
        </view>
      </view>

      <!-- 只读表单区 -->
      <view class="section-card">
        <view class="section-title">表单详情</view>
        <view class="info-row" v-for="widget in visibleWidgetList" :key="widget.config.code">
          <text class="info-label">{{ widget.config.label }}</text>
          <text class="info-value">{{ widgetValue(widget) }}</text>
        </view>
        <view v-if="visibleWidgetList.length === 0" class="form-empty">
          <text>暂无表单数据</text>
        </view>
      </view>

      <!-- 审批记录区 -->
      <view class="section-card">
        <view class="section-title">审批记录</view>
        <view class="node-item" v-for="(node, index) in nodes" :key="node.id || index">
          <view class="node-header">
            <text class="node-name">{{ node.name }}</text>
            <text class="node-status" :class="'status-' + node.status">{{ node.statusName }}</text>
          </view>
          <view v-if="node.status === 2" class="node-waiter">待审批人：{{ node.unAssigneeName || '-' }}</view>
          <view class="discuss-item" v-for="(discuss, dIndex) in node.discusses" :key="discuss.id || dIndex">
            <view class="discuss-header">
              <text class="discuss-name">{{ discuss.assigneeName }}</text>
              <text class="discuss-status" :class="'status-' + discuss.status">{{ discuss.statusName }}</text>
              <text class="discuss-time">{{ discuss.createdDt }}</text>
            </view>
            <text v-if="discuss.discuss" class="discuss-content">{{ discuss.discuss }}</text>
          </view>
        </view>
        <view v-if="nodes.length === 0" class="form-empty">
          <text>暂无审批记录</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="detail-footer" v-if="showFooter">
      <!-- 我的待办：通过/驳回/加签/转办/抄送 -->
      <template v-if="type === 3">
        <up-button v-if="hasPermission(1)" type="primary" size="small" text="通过" @click="openApprovalDialog(3)"/>
        <up-button v-if="hasPermission(2)" type="error" size="small" text="驳回" @click="openApprovalDialog(4)"/>
        <up-button v-if="hasPermission(32)" type="warning" size="small" text="加签" @click="openUserPicker('addSign')"/>
        <up-button v-if="hasPermission(16)" type="info" size="small" plain text="转办" @click="openUserPicker('transfer')"/>
        <up-button v-if="hasPermission(64)" type="info" size="small" text="抄送" @click="openUserPicker('copy')"/>
      </template>
      <!-- 我的申请：催办/撤回 -->
      <template v-else-if="type === 1">
        <up-button v-if="instance.status === 2 && hasPermission(8)" type="warning" size="small" text="催办" :loading="urging" @click="handleUrge"/>
        <up-button v-if="instance.status === 2 && isCreator && hasPermission(128)" type="error" size="small" text="撤回" :loading="recalling" @click="handleRecall"/>
      </template>
    </view>

    <!-- 审批意见弹窗 -->
    <up-modal
        :show="approvalVisible"
        :title="approvalStatus === 3 ? '通过' : '驳回'"
        showCancelButton
        @confirm="handleApproval"
        @cancel="approvalVisible = false"
    >
      <view class="modal-body">
        <up-textarea v-model="approvalDiscuss" placeholder="请输入审批意见" count :maxlength="200"/>
      </view>
    </up-modal>

    <!-- 加签/转办/抄送选人弹层（复用选择用户组件） -->
    <user-picker
        v-model:show="userPickerVisible"
        :title="operateType === 'transfer' ? '选择转办人' : operateType === 'copy' ? '选择抄送人' : '选择加签人'"
        :multiple="operateType !== 'transfer'"
        :loading="operating"
        @confirm="handleUserConfirm"
    />
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
import store from "@/store/index.js";
import {useDebounceFn} from "@/utils/debounce";

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
    // 给模板字段回填值（value/showValue）
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
    // 审批中：催办（有权限）或撤回（发起人+有撤回权限）
    if (instance.value.status !== 2) return false;
    return (nodeId.value !== undefined && hasPermission(8)) || (isCreator.value && hasPermission(128));
  }
  return false;
});

/**
 * 判断节点操作权限
 * @param bit 权限位 1-通过 2-驳回 8-催办 16-转办 32-加签
 */
function hasPermission(bit) {
  return ((nodePermission.value || 0) & bit) === bit;
}

/**
 * 表单组件取值：优先展示名称文本
 * @param widget 表单组件
 */
function widgetValue(widget) {
  const value = (widget.config.showValue !== undefined && widget.config.showValue !== null && widget.config.showValue !== '')
      ? widget.config.showValue : widget.config.value;
  return value === undefined || value === null || value === '' ? '-' : value;
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
 * 催办流程实例
 */
const handleUrge = useDebounceFn(() => {
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
</script>
<style scoped lang="scss">
.detail-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.detail-container :deep(.u-popup) {
  flex: none;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background-color: #0052cc;

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.detail-body {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 10px 12px;
  box-sizing: border-box;
}

.section-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    padding-bottom: 4px;
    border-bottom: 1px solid #ebeef5;
  }
}

.info-row {
  display: flex;
  font-size: 13px;

  .info-label {
    color: #909399;
    width: 80px;
    flex-shrink: 0;
  }

  .info-value {
    flex: 1;
    color: #606266;
    word-break: break-all;
  }
}

.form-empty {
  display: flex;
  justify-content: center;
  padding: 10px 0;
  font-size: 13px;
  color: #909399;
}

// 审批节点
.node-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;

  .node-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .node-name {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }

    .node-status {
      font-size: 12px;
    }
  }

  .node-waiter {
    font-size: 12px;
    color: #909399;
  }
}

.discuss-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 10px;
  border-radius: 6px;
  background-color: #f5f7fa;

  .discuss-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;

    .discuss-name {
      color: #303133;
    }

    .discuss-time {
      color: #909399;
    }
  }

  .discuss-content {
    font-size: 12px;
    color: #606266;
    line-height: 1.6;
    word-break: break-all;
  }
}

// 状态着色：审批中蓝、通过绿、驳回红、撤回橙、转办/加签灰
.status-1,
.status-6,
.status-7,
.status-8 {
  color: #909399;
}

.status-2 {
  color: #0052cc;
}

.status-3,
.status-5 {
  color: #52c41a;
}

.status-4 {
  color: #f56c6c;
}

.status-9 {
  color: #e6a23c;
}

.detail-footer {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
  background-color: #ffffff;
  border-top: 1px solid #ebeef5;

  :deep(.u-button) {
    flex: 1;
  }
}

.modal-body {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 0;
}
</style>
