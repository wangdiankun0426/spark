<template>
    <div class="app-container">
      <!--查询条件-->
      <el-form :model="formQuery" label-width="auto">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-button
                type="primary"
                @click="handleOpenCreateFormForm"
            >
              <el-icon><Plus /></el-icon>新建表单模板
            </el-button>
            <el-button
                type="warning"
                @click="handleResetFormQuery"
            >
              <el-icon><Refresh /></el-icon>重置
            </el-button>
            <el-button
                type="info"
                @click="handleGetFormList"
            >
              <el-icon><Search /></el-icon>查询
            </el-button>
          </el-col>
        </el-row>
      </el-form>
      <!--表单列表-->
      <el-table
          height="calc(100vh - 155px)"
          ref="tableRef"
          :data="formList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="名称" sortable="custom" width="200" align="center">
          <template #header>
            名称
            <el-popover
                :visible="formSearchFlag.name"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="formSearchFlag.name ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="formSearchFlag.name = !formSearchFlag.name"
                />
              </template>
              <div>
                <el-input
                    v-model="formQuery.name"
                    placeholder="请输入名称"
                    clearable
                    @input="handleGetFormList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="revNum" label="版本号" align="center">
          <template #default="scope">
            <el-button
                type='primary'
                text
                @click="handleGetFormVersionList(scope.row.id)"
            >
              {{scope.row.revNum}}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="表单类型" align="center"/>
        <el-table-column prop="orderNum" label="排序号" sortable="custom" align="center"/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="scope">
            <el-button
                type="primary"
                text
                @click="handleOpenFormDesigner(scope.row)"
            >
              <el-icon><EditPen /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
                编辑模版
              </span>
            </el-button>
            <el-button
                type="success"
                text
                @click="handleOpenUpdateFormForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
             修改
            </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDeleteForm(scope.row.id)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
             删除
            </span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!--分页组件-->
      <div>
        <el-pagination
            :current-page="formQuery.pageNo"
            :page-size="formQuery.pageSize"
            :page-sizes="pageSizes"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handlePageChangeSize"
            @current-change="handlePageChangeNo"
        />
      </div>
    </div>

    <!--表单表单-->
    <el-drawer
        v-model="formFormVisible"
        :title="formFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseFormForm"
    >
      <el-form
          :model="formForm"
          label-width="auto"
          :rules="formFormRules"
          ref="formFormRef"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input
                  v-model="formForm.name"
                  placeholder="请输入名称"
              />
            </el-form-item>
            <el-form-item label="表单类型" prop="type">
              <el-select
                  v-model="formForm.type"
                  placeholder="请选择表单类型"
                  :disabled="!!formForm.id"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in formTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="排序号" prop="orderNum">
              <el-input-number
                  v-model="formForm.orderNum"
                  :min="0"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%"
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
              <div>普通表单：通用数据采集表单，字段可自由配置。</div>
              <div>流程表单：流程表单，字段可自由配置。</div>
              <div>元数据表单：用于知识库文档元数据管理，由系统按文档结构调用。</div>
              <div class="form-tip-warn">表单类型一经创建不可修改，请谨慎选择。</div>
            </div>
          </template>
        </el-alert>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button
              type="primary"
              @click="handleSubmitFormForm"
          >保存</el-button>
          <el-button
              @click="handleCloseFormForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
    <!--表单版本列表-->
    <el-drawer
        v-model="formVersionListVisible"
        title="表单版本"
        direction="ltr"
        size="20%"
    >
      <div>
        <el-table
            height="calc(100vh - 140px)"
            ref="tableFormVersionRef"
            :data="formVersionList"
        >
          <el-table-column prop="id" label="编号" align="center"/>
          <el-table-column prop="revNum" label="版本号" align="center"/>
          <el-table-column fixed="right" label="">
            <template #default="scope">
              <el-tooltip content="表单模版" placement="bottom"  >
                <el-button
                    type="primary"
                    circle
                    @click="handleOpenFormDesignerV2(scope.row)"
                >
                  <el-icon><EditPen /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!--分页组件-->
      <div>
        <el-pagination
            :current-page="formVersionQuery.pageNo"
            :page-size="formVersionQuery.pageSize"
            :page-sizes="pageVersionSizes"
            :background="true"
            layout="total, sizes, prev, next"
            :total="versionTotal"
            @size-change="handleVersionPageChangeSize"
            @current-change="handleVersionPageChangeNo"
        />
      </div>
    </el-drawer>
</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageFormListAPI, createFormAPI, updateFormAPI, queryFormDetailAPI, deleteFormAPI} from '@/api/form/form';
import {pageFormVersionListAPI} from '@/api/form/formVersion';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';
const { proxy } = getCurrentInstance();
const formQuery = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  sorts: {},
})
const formSearchFlag = ref({
  name: false,
})
const total = ref(0)
const pageSizes = [30,50,100];
const formList = ref([]);
const formFormVisible = ref(false)
const formFormTitle = ref('')
const formForm = ref({
  id: undefined,
  name: undefined,
  type: undefined,
  orderNum: undefined,
})
const formTypeOptions = [
  { value: 1, label: '普通表单' },
  { value: 2, label: '流程表单' },
  { value: 3, label: '元数据表单' },
  { value: 4, label: 'workflow表单' },
]
const formFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入名称" }],
  type: [{ required: true, trigger: "change", message: "请选择表单类型" }],
};
const formVersionListVisible = ref(false);
const formVersionList = ref([]);
const formVersionQuery = ref({
  pageNo: 1,
  pageSize: 30,
  formId: undefined,
});
const versionTotal = ref(0);
const pageVersionSizes = [15,30,50];

handleGetFormList();

/**
 * 查询表单版本列表
 * */
function handleGetFormVersionList(formId) {
  formVersionQuery.value.formId = formId;
  pageFormVersionListAPI(formVersionQuery.value).then(res => {
    formVersionList.value = res.data.rows;
    versionTotal.value = res.data.total;
    formVersionListVisible.value = true;
  });
}

/**
 * 删除表单
 * @param id
 * */
function handleDeleteForm(id) {
  ElMessageBox.confirm(
      '是否确定删除此条表单?',
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
    deleteFormAPI(data).then(res => {
      handleGetFormList();
      ElMessage.success("删除表单成功");
    })
  }).catch(() => {})
};

/**
 * 打开修改表单表单
 * @param row
 */
function handleOpenUpdateFormForm(row) {
  const query = {
    id: row.id
  };
  queryFormDetailAPI(query).then(res => {
    formForm.value.id = res.data.id;
    formForm.value.name = res.data.name;
    formForm.value.type = res.data.type;
    formForm.value.orderNum = res.data.orderNum;
    formFormTitle.value = "修改表单";
    formFormVisible.value = true;
  });
};

/**
 * 打开表单设计器
 * */
function handleOpenFormDesigner(row) {
  window.open("/form/designer/"+row.id+"/"+row.revId);
}

/**
 * 打开表单设计器
 * */
function handleOpenFormDesignerV2(row) {
  window.open("/form/designer/"+row.formId+"/"+row.id);
}

/**
 * 打开创建表单表单
 * */
function handleOpenCreateFormForm() {
  formForm.value.id = undefined;
  formForm.value.name = undefined;
  formForm.value.type = undefined;
  formForm.value.orderNum = 99;
  formFormTitle.value = "创建表单";
  formFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetFormQuery() {
  formQuery.value.pageNo = 1;
  formQuery.value.pageSize = 15;
  formQuery.value.name = undefined;
  formSearchFlag.value.name = false;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  formQuery.value.sorts = {};
  handleGetFormList();
}

/**
 * 提交表单表单
 * */
function handleSubmitFormForm() {
  proxy.$refs.formFormRef.validate(valid => {
    if (valid) {
      if (!formForm.value.id) {
        const data = {
          name: formForm.value.name,
          type: formForm.value.type,
          orderNum: formForm.value.orderNum,
        };
        createFormAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("表单创建成功");
          handleCloseFormForm();
          handleGetFormList();
        })
      } else {
        const data = {
          id:  formForm.value.id,
          name: formForm.value.name,
          type: formForm.value.type,
          orderNum: formForm.value.orderNum,
        };
        updateFormAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("表单修改成功");
          handleCloseFormForm();
          handleGetFormList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseFormForm() {
  formForm.value.id = undefined;
  formForm.value.name = undefined;
  formForm.value.type = undefined;
  formForm.value.orderNum = undefined;
  formFormTitle.value = "";
  formFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetFormList() {
  pageFormListAPI(formQuery.value).then(res => {
    formList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  formQuery.value.pageSize = pageSize;
  handleGetFormList();
};

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  formQuery.value.pageNo = pageNo;
  handleGetFormList();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handleVersionPageChangeSize(pageSize) {
  formVersionQuery.value.pageSize = pageSize;
  handleGetFormVersionList();
};

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handleVersionPageChangeNo(pageNo) {
  formVersionQuery.value.pageNo = pageNo;
  handleGetFormVersionList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = formQuery.value.sorts[property];
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
    formQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    formQuery.value.sorts[prop] = 'desc';
  } else {
    formQuery.value.sorts[prop] = null;
  }
  handleGetFormList();
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
