<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="warning"
          @click="handleResetInstanceQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetInstanceList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--流程实例列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 165px)"
          :data="instanceList"
          highlight-current-row
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="processId" label="模板ID"  align="center" width="200px"/>
        <el-table-column prop="statusName" label="状态"  align="center"/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" align="center"/>
        <el-table-column fixed="right" label="" width="操作">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="handleOpenInstance(scope.row.id)"
            >
              <el-icon><View /></el-icon>查看流程
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="instanceQuery.pageNo"
          :page-size="instanceQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>

    <!--流程实例表单-->
    <el-drawer
        v-model="flowDetailVisible"
        title="流程详情"
        direction="rtl"
        size="80%"
        :before-close="handleCloseFlowDetail"
    >
      <el-tabs v-model="detailActiveTab">
        <el-tab-pane label="表单详情" name="form">
          <form-view
              :form="formJson"
              v-if="flowDetailVisible && detailActiveTab === 'form'"
          />
        </el-tab-pane>
        <el-tab-pane label="流程图" name="flow">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="timeline-container">
                <h4>流程时间线</h4>
                <el-timeline>
                  <el-timeline-item
                      v-for="(node, index) in nodes"
                      :key="index"
                      :type="node.status === 2 ? 'primary' : (node.status === 3 ? 'success' : 'danger')"
                  >
                    <p>{{ node.name }}</p>
                    <p>{{ node.createdDt }}</p>
                    <p v-if="node.type === 'userTask'">节点状态：{{ node.statusName }}</p>
                    <p v-if="node.status === 2">待审批人：{{ node.unAssigneeName }}</p>
                    <p v-for="(discuss, index) in node.discusses" :key="index" style="font-size: 12px">
                      <p>{{ discuss.assigneeName }} - {{ discuss.createdDt }}<br>
                        审批意见: {{ discuss.discuss }}
                      </p>
                    </p>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-col>
            <el-col :span="18">
              <form-view
                  :bpmJson="bpmJson"
                  v-if="flowDetailVisible && detailActiveTab === 'flow'"
              />
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>

  </div>
</template>

<script setup>
import {ref} from 'vue';
import {
  pageMyPendedListAPI,
  showInstanceDetailAPI
} from '@/api/flow/instance';
import { Search } from '@element-plus/icons-vue';
import FormView from '@/components/FormView';
import FlowView from '@/components/FlowView';

const instanceQuery = ref({
  pageNo: 1,
  pageSize: 30,
});
const total = ref(0);
const pageSizes = [30,50,100];
const instanceList = ref([]);

handleGetInstanceList();

/**
 * 重置查询条件
 * */
function handleResetInstanceQuery() {
  instanceQuery.value.pageNo = 1;
  instanceQuery.value.pageSize = 15;
  handleGetInstanceList();
}

/**
 * 关闭流程详情抽屉
 * */
function handleCloseFlowDetail() {
  formJson.value = {};
  bpmJson.value = {};
  nodes.value = [];
  nodeId.value = undefined;
  instanceId.value = undefined;
  flowDetailVisible.value = false;
  detailActiveTab.value = 'form';
}

/**
 * 查询列表
 */
function handleGetInstanceList() {
  pageMyPendedListAPI(instanceQuery.value).then(res => {
    if (res.data !== undefined) {
      instanceList.value = res.data.rows;
      total.value = res.data.total;
    } else {
      instanceList.value = [];
      total.value = 0;
    }
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  instanceQuery.value.pageSize = pageSize;
  handleGetInstanceList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  instanceQuery.value.pageNo = pageNo;
  handleGetInstanceList();
}

const formJson = ref({});
const bpmJson = ref({});
const flowDetailVisible = ref(false);
const detailActiveTab = ref('form');
const nodes = ref([]);
const nodeId = ref(undefined);
const instanceId = ref(undefined);

/**
 * 打开流程模板
 * @param id
 */
function handleOpenInstance(id) {
  const query = {
    id: id,
  }
  showInstanceDetailAPI(query).then(res => {
    if (res.code === 200) {
      formJson.value = JSON.parse(res.data.formJson);
      bpmJson.value = JSON.parse(res.data.bpmJson);
      nodes.value = res.data.nodes;
      instanceId.value = res.data.id;
      nodeId.value = res.data.nodeId;
      const values = res.data.values;
      if (values.length === 0) {
        flowDetailVisible.value = true;
        return ;
      }
      // 给模板中的字段赋值
      const codes = values.map(value => value.code);
      formJson.value.widgetList.forEach(widget => {
        const index = codes.indexOf(widget.config.code);
        if (index === -1) {
          return;
        }
        const value = values[index].value;
        const showValue = values[index].showValue;
        widget.config.value = value;
        widget.config.showValue = showValue;
      })
      flowDetailVisible.value = true;
    }
  }).catch(e => {})
}
</script>

<style scoped>
.timeline-container {
  padding: 5px;
  border-right: 1px solid #ebeef5;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.timeline-container h4 {
  margin-left: 20px;
  margin-bottom: 10px;
  color: #333;
}
</style>

