<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenCreateModelForm"
      >
        <el-icon><Plus /></el-icon>新建模型
      </el-button>
      <el-button
          type="warning"
          @click="handleResetModelQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetModelList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--模型列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="modelList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="providerName" label="供应商" width="200" align="center">
          <template #header>
            供应商
            <el-popover
                :visible="modelSearchFlag.providerId"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="modelSearchFlag.providerId ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="modelSearchFlag.providerId = !modelSearchFlag.providerId"
                />
              </template>
              <div>
                <el-select
                    v-model="modelQuery.providerId"
                    placeholder="请选择供应商"
                    clearable
                    @change="handleGetModelList"
                >
                  <el-option
                      v-for="item in providerList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                  />
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="模型类型" width="120" align="center">
          <template #header>
            模型类型
            <el-popover
                :visible="modelSearchFlag.type"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="modelSearchFlag.type ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="modelSearchFlag.type = !modelSearchFlag.type"
                />
              </template>
              <div>
                <el-select
                    v-model="modelQuery.type"
                    placeholder="请选择模型类型"
                    clearable
                    @change="handleGetModelList"
                >
                  <el-option
                      v-for="item in modelTypeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="模型名称" width="200" align="center">
          <template #header>
            模型名称
            <el-popover
                :visible="modelSearchFlag.name"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="modelSearchFlag.name ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="modelSearchFlag.name = !modelSearchFlag.name"
                />
              </template>
              <div>
                <el-input
                    v-model="modelQuery.name"
                    placeholder="请输入模型名称"
                    clearable
                    @input="handleGetModelList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="enableThinking" label="思考模式" width="100" align="center">
          <template #default="scope">
            {{ scope.row.enableThinking === 1 ? '开启' : '未开启' }}
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="温度参数" width="100" align="center"/>
        <el-table-column prop="statusName" label="状态" width="100" align="center"/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="140" align="center">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="handleOpenUpdateModelForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDeleteModel(scope.row.id)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
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
          :current-page="modelQuery.pageNo"
          :page-size="modelQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--模型表单-->
    <el-drawer
        v-model="modelFormVisible"
        :title="modelFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseModelForm"
    >
      <el-form
          :model="modelForm"
          label-width="auto"
          :rules="modelFormRules"
          ref="modelFormRef"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="供应商" prop="providerId">
              <el-select
                  v-model="modelForm.providerId"
                  placeholder="请选择模型供应商，如：DeepSeek"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in providerList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="模型类型" prop="type">
              <el-select
                  v-model="modelForm.type"
                  placeholder="请选择模型类型"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in modelTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="模型名称" prop="name">
              <el-input
                  v-model="modelForm.name"
                  placeholder="请输入模型名称，如：gpt-4、claude-3-opus"
                  maxlength="50"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="思考模式" prop="enableThinking" v-if="modelForm.type === 1">
              <el-switch
                  v-model="modelForm.enableThinking"
                  :active-value="1"
                  :inactive-value="-1"
                  active-text="已启用"
                  inactive-text="已停用"
                  inline-prompt
              />
            </el-form-item>
            <el-form-item label="温度参数" prop="temperature" v-if="modelForm.type === 1">
              <el-input-number
                  v-model="modelForm.temperature"
                  :min="0"
                  :max="2"
                  :step="0.01"
                  :precision="2"
                  style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-switch
                  v-model="modelForm.status"
                  :active-value="1"
                  :inactive-value="-1"
                  active-text="已启用"
                  inactive-text="已停用"
                  inline-prompt
              />
            </el-form-item>
            <el-form-item label="模型描述" prop="description">
              <el-input
                  v-model="modelForm.description"
                  placeholder="请输入模型描述，简要说明模型能力与适用场景，最多 200 字"
                  type="textarea"
                  :rows="5"
                  maxlength="256"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                  v-model="modelForm.remark"
                  placeholder="请输入备注信息，记录其他需要说明的事项，最多 200 字"
                  type="textarea"
                  :rows="5"
                  maxlength="256"
                  show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <!--必填字段填写提示-->
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>供应商为必填项，请先在模型厂商管理中维护；</div>
            <div>模型类型决定模型在系统中的用途：语言模型用于对话生成，向量模型用于文本向量化检索，排序模型用于对召回结果重排；</div>
            <div>模型名称为必填项，需与供应商官方模型标识保持一致；</div>
            <div>模型密钥已迁移至厂商管理，请在厂商管理中维护；</div>
            <div>思考模式用于语言模型深度推理场景，向量模型无需开启；</div>
            <div>温度参数控制生成内容随机性，范围 0.0-2.0，值越低输出越稳定，值越高越发散，建议 0.1-0.7；</div>
            <div>模型描述与备注用于辅助识别，建议填写模型能力范围与使用注意事项。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button
              type="primary"
              @click="handleSubmitModelForm"
          >保存</el-button>
          <el-button
              @click="handleCloseModelForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {getCurrentInstance, ref, onMounted} from 'vue';
import {pageModelListAPI, createModelAPI, updateModelAPI, queryModelDetailAPI, deleteModelAPI} from '@/api/llm/model.js';
import {pageProviderListAPI} from '@/api/llm/provider.js';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const modelQuery = ref({
  pageNo: 1,
  pageSize: 30,
  providerId: undefined,
  type: undefined,
  name: undefined,
  sorts: {},
});
const modelSearchFlag = ref({
  providerId: false,
  type: false,
  name: false,
});
const total = ref(0);
const pageSizes = [30,50,100];
const modelList = ref([]);
const modelFormVisible = ref(false);
const modelFormTitle = ref('');
const modelForm = ref({
  id: undefined,
  providerId: undefined,
  type: undefined,
  name: undefined,
  enableThinking: -1,
  temperature: 0.10,
  status: -1,
  description: undefined,
  remark: undefined,
});
const modelFormRules = {
  providerId: [{ required: true, trigger: "change", message: "请选择供应商" }],
  type: [{ required: true, trigger: "change", message: "请选择模型类型" }],
  name: [{ required: true, trigger: "blur", message: "请输入模型名称" }],
  temperature: [
    { required: true, message: "请输入温度参数", trigger: "blur" },
    { type: "number", min: 0, max: 2, message: "温度参数范围为 0.0-2.0", trigger: "blur" }
  ],
};

// 模型类型枚举
const modelTypeOptions = [
  { label: '语言模型', value: 1 },
  { label: '向量模型', value: 2 },
  { label: '排序模型', value: 3 }
];

// 供应商列表
const providerList = ref([]);

// 获取供应商列表
function handleGetProviderList() {
  pageProviderListAPI({ pageNo: 1, pageSize: 1000 }).then(res => {
    providerList.value = res.data.rows || [];
  });
}

handleGetModelList();
handleGetProviderList();

/**
 * 删除模型
 * @param id
 * */
function handleDeleteModel(id) {
  ElMessageBox.confirm(
      '是否确定删除此条模型?',
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
    deleteModelAPI(data).then(res => {
      handleGetModelList();
      ElMessage.success("删除模型成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改模型表单
 * @param row
 */
function handleOpenUpdateModelForm(row) {
  const query = {
    id: row.id
  };
  queryModelDetailAPI(query).then(res => {
    modelForm.value.id = res.data.id;
    modelForm.value.providerId = res.data.providerId;
    modelForm.value.type = res.data.type;
    modelForm.value.name = res.data.name;
    modelForm.value.enableThinking = res.data.enableThinking;
    modelForm.value.temperature = res.data.temperature;
    modelForm.value.status = res.data.status;
    modelForm.value.description = res.data.description;
    modelForm.value.remark = res.data.remark;
    modelFormTitle.value = "修改模型";
    modelFormVisible.value = true;
  });
}

/**
 * 打开创建模型表单
 * */
function handleOpenCreateModelForm() {
  modelForm.value.id = undefined;
  modelForm.value.providerId = undefined;
  modelForm.value.type = undefined;
  modelForm.value.name = undefined;
  modelForm.value.enableThinking = -1;
  modelForm.value.temperature = 0.10;
  modelForm.value.status = 1;
  modelForm.value.description = undefined;
  modelForm.value.remark = undefined;
  modelFormTitle.value = "创建模型";
  modelFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetModelQuery() {
  modelQuery.value.pageNo = 1;
  modelQuery.value.pageSize = 15;
  modelQuery.value.providerId = undefined;
  modelSearchFlag.value.providerId = false;
  modelQuery.value.type = undefined;
  modelSearchFlag.value.type = false;
  modelQuery.value.name = undefined;
  modelSearchFlag.value.name = false;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  modelQuery.value.sorts = {};
  handleGetModelList();
}

/**
 * 提交模型表单
 * */
function handleSubmitModelForm() {
  proxy.$refs.modelFormRef.validate(valid => {
    if (valid) {
      if (!modelForm.value.id) {
        const data = {
          providerId: modelForm.value.providerId,
          type: modelForm.value.type,
          name: modelForm.value.name,
          enableThinking: modelForm.value.type === 1 ? modelForm.value.enableThinking : -1,
          temperature: modelForm.value.temperature,
          status: modelForm.value.status,
          description: modelForm.value.description,
          remark: modelForm.value.remark,
        };
        createModelAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("模型创建成功");
          handleCloseModelForm();
          handleGetModelList();
        })
      } else {
        const data = {
          id:  modelForm.value.id,
          providerId: modelForm.value.providerId,
          type: modelForm.value.type,
          name: modelForm.value.name,
          enableThinking: modelForm.value.type === 1 ? modelForm.value.enableThinking : -1,
          temperature: modelForm.value.temperature,
          status: modelForm.value.status,
          description: modelForm.value.description,
          remark: modelForm.value.remark,
        };
        updateModelAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("模型修改成功");
          handleCloseModelForm();
          handleGetModelList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseModelForm() {
  modelForm.value.id = undefined;
  modelForm.value.providerId = undefined;
  modelForm.value.type = undefined;
  modelForm.value.name = undefined;
  modelForm.value.enableThinking = -1;
  modelForm.value.temperature = 0.10;
  modelForm.value.status = 1;
  modelForm.value.description = undefined;
  modelForm.value.remark = undefined;
  modelFormTitle.value = "";
  modelFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetModelList() {
  pageModelListAPI(modelQuery.value).then(res => {
    modelList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  modelQuery.value.pageSize = pageSize;
  handleGetModelList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  modelQuery.value.pageNo = pageNo;
  handleGetModelList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = modelQuery.value.sorts[property];
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
    modelQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    modelQuery.value.sorts[prop] = 'desc';
  } else {
    modelQuery.value.sorts[prop] = null;
  }
  handleGetModelList();
}
</script>

<style scoped lang="scss">
.form-tip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  line-height: 1.6;
}

.drawer-footer {
  padding: 0 16px;
}
</style>
