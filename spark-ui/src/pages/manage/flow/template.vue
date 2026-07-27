<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenCreateTemplateForm"
      >
        <el-icon><Plus /></el-icon>新建
      </el-button>
      <el-button
          type="warning"
          @click="handleResetTemplateQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetTemplateList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--流程模板列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 165px)"
          :data="templateList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
          border
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="名称" width="200" align="center">
          <template #header>
            名称
            <el-popover
                :visible="templateSearchFlag.name"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="templateSearchFlag.name ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="templateSearchFlag.name = !templateSearchFlag.name"
                />
              </template>
              <div>
                <el-input
                    v-model="templateQuery.name"
                    placeholder="请输入名称"
                    clearable
                    @input="handleGetTemplateList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="processId" label="模板ID"  align="center" width="200px">
          <template #header>
            模板ID
            <el-popover
                :visible="templateSearchFlag.processId"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="templateSearchFlag.processId ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="templateSearchFlag.processId = !templateSearchFlag.processId"
                />
              </template>
              <div>
                <el-input
                    v-model="templateQuery.processId"
                    placeholder="请输入模板id"
                    clearable
                    @input="handleGetTemplateList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="revNum" label="版本"  align="center"/>
        <el-table-column prop="statusName" label="状态"  align="center">
          <template #header>
            状态
            <el-popover
                :visible="templateSearchFlag.status"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="templateSearchFlag.status ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="templateSearchFlag.status = !templateSearchFlag.status"
                />
              </template>
              <div>
                <el-select
                    clearable
                    v-model="templateQuery.status"
                    @change="handleGetTemplateList"
                    placeholder="请选择状态"
                >
                  <el-option
                      v-for="item in statusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                      :disabled="item.disabled"
                  >
                  </el-option>
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="scope">
            <el-button
                type="primary"
                text
                @click="handleOpenEditTemplate(scope.row)"
            >
              <el-icon><EditPen /></el-icon>
              <span style="font-size: 12px; font-weight: 500">
                编辑模版
              </span>
            </el-button>
            <el-button
                type="success"
                text
                @click="handleOpenUpdateTemplateForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 500">
                修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDeleteTemplate(scope.row.id)"
                v-if="scope.row.status === -1"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 500">
                删除
              </span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="templateQuery.pageNo"
          :page-size="templateQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--流程模板表单-->
    <el-drawer
        v-model="templateFormVisible"
        :title="templateFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseTemplateForm"
    >
      <el-form
          :model="templateForm"
          label-width="auto"
          :rules="templateFormRules"
          ref="templateFormRef"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input
                  v-model="templateForm.name"
                  placeholder="请输入名称"
              />
            </el-form-item>
            <el-form-item label="表单" prop="formId">
              <el-select v-model="templateForm.formId" placeholder="请选择流程表单" style="width: 100%">
                <el-option
                    v-for="item in formOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-switch
                  v-model="templateForm.status"
                  :active-value="1"
                  :inactive-value="-1"
                  active-text="已启用"
                  inactive-text="已停用"
                  inline-prompt
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <!--底部提示-->
      <div class="form-tip">
        <el-alert
            type="info"
            :closable="false"
            show-icon
        >
          <template #title>
            <div class="form-tip-content">
              <div>已开启：流程模板可用，可发起新的流程实例。</div>
              <div>已停用：流程模板停用，无法发起新的流程实例，已有实例不受影响。</div>
              <div class="form-tip-warn">仅状态为"已停用"的流程模板可被删除，请谨慎操作。</div>
            </div>
          </template>
        </el-alert>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button
              type="primary"
              @click="handleSubmitTemplateForm"
          >保存</el-button>
          <el-button
              @click="handleCloseTemplateForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageTemplateListAPI, createTemplateAPI, updateTemplateAPI,
  queryTemplateDetailAPI, deleteTemplateAPI} from '@/api/flow/template';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';
import {queryFormListAPI} from "@/api/form/form.js";

const { proxy } = getCurrentInstance();
const templateQuery = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  processId: undefined,
  status: undefined,
  sorts: {},
});
const templateSearchFlag = ref({
  name: false,
  processId: false,
  status: false,
});
const total = ref(0);
const pageSizes = [30,50,100];
const templateList = ref([]);
const templateFormVisible = ref(false);
const templateFormTitle = ref('');
const templateForm = ref({
  id: undefined,
  name: undefined,
});
const templateFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入名称" }],
  formId: [{ required: true, trigger: "change", message: "请选择流程表单" }],
  status: [{ required: true, trigger: "change", message: "请选择状态" }],
};

const statusOptions = [
  {
    label: "已停用",
    value: -1,
  },
  {
    label: "已开启",
    value: 1,
  },
]

handleGetTemplateList();

/**
 * 删除流程模板
 * @param id
 * */
function handleDeleteTemplate(id) {
  ElMessageBox.confirm(
      '是否确定删除此条流程模板?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      id: id
    };
    deleteTemplateAPI(data).then(res => {
      handleGetTemplateList();
      ElMessage.success("删除流程模板成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改流程模板表单
 * @param row
 */
function handleOpenUpdateTemplateForm(row) {
  const query = {
    id: row.id
  };
  queryTemplateDetailAPI(query).then(res1 => {
    const query = {
      type: 2,
      page: false
    };
    queryFormListAPI(query).then(res => {
      if (res.code === 200 && res.data !== undefined) {
        formOptions.value = res.data.map(item => {
          return {
            label: item.name,
            value: item.id
          }
        });
      }
      templateForm.value.id = res1.data.id;
      templateForm.value.name = res1.data.name;
      templateForm.value.formId = res1.data.formId;
      templateForm.value.status = res1.data.status;
      templateFormTitle.value = "修改流程模板";
      templateFormVisible.value = true;
    }).catch(e => {})
  });
}

/**
 * 打开编辑流程模板
 * @param row
 */
const handleOpenEditTemplate = (row) => {
  window.open("/flow/designer/"+row.id+"/"+(row.revId === undefined ? 0 : row.revId));
}

const formOptions = ref([]);

/**
 * 打开创建流程模板表单
 * */
function handleOpenCreateTemplateForm() {
  const query = {
    type: 2,
    page: false
  };
  queryFormListAPI(query).then(res => {
    if (res.code === 200 && res.data !== undefined) {
      formOptions.value = res.data.map(item => {
        return {
          label: item.name,
          value: item.id
        }
      });
    }
    templateForm.value.id = undefined;
    templateForm.value.name = undefined;
    templateForm.value.formId = undefined;
    templateForm.value.status = 1;
    templateFormTitle.value = "创建流程模板";
    templateFormVisible.value = true;
  }).catch(e => {})
}

/**
 * 重置查询条件
 * */
function handleResetTemplateQuery() {
  templateQuery.value.pageNo = 1;
  templateQuery.value.pageSize = 15;
  templateQuery.value.name = undefined;
  templateSearchFlag.value.name = false;
  templateQuery.value.processId = undefined;
  templateSearchFlag.value.processId = false;
  templateQuery.value.status = undefined;
  templateSearchFlag.value.status = false;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  templateQuery.value.sorts = {};
  handleGetTemplateList();
}

/**
 * 提交流程模板表单
 * */
function handleSubmitTemplateForm() {
  proxy.$refs.templateFormRef.validate(valid => {
    if (valid) {
      if (!templateForm.value.id) {
        const data = {
          name: templateForm.value.name,
          processId: templateForm.value.processId,
          revNum: templateForm.value.revNum,
          status: templateForm.value.status,
          formId: templateForm.value.formId,
        };
        createTemplateAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("流程模板创建成功");
          handleCloseTemplateForm();
          handleGetTemplateList();
        })
      } else {
        const data = {
          id:  templateForm.value.id,
          name: templateForm.value.name,
          processId: templateForm.value.processId,
          revNum: templateForm.value.revNum,
          status: templateForm.value.status,
          formId: templateForm.value.formId,
        };
        updateTemplateAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("流程模板修改成功");
          handleCloseTemplateForm();
          handleGetTemplateList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseTemplateForm() {
  templateForm.value.id = undefined;
  templateForm.value.name = undefined;
  templateForm.value.processId = undefined;
  templateForm.value.revNum = undefined;
  templateForm.value.status = undefined;
  templateFormTitle.value = "";
  templateFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetTemplateList() {
  pageTemplateListAPI(templateQuery.value).then(res => {
    templateList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  templateQuery.value.pageSize = pageSize;
  handleGetTemplateList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  templateQuery.value.pageNo = pageNo;
  handleGetTemplateList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = templateQuery.value.sorts[property];
  if (order === 'asc') {
    data.column.order = 'ascending';
  } else if (order === 'desc') {
    data.column.order = 'descending';
  } else {
    data.column.order = null;
  }
}

/**
 * 处理排序
 * @param column
 * @param prop
 * @param order
 */
function handleSortChange({ column, prop, order }) {
  if (order === 'ascending') {
    templateQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    templateQuery.value.sorts[prop] = 'desc';
  } else {
    templateQuery.value.sorts[prop] = null;
  }
  handleGetTemplateList();
}
</script>

<style scoped>
.form-tip {
  margin-top: 16px;
}
.form-tip-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  line-height: 1.6;
}
.form-tip-warn {
  color: #e6a23c;
  margin-top: 4px;
}
</style>
