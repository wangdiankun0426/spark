<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenCreateProviderForm"
      >
        <el-icon><Plus /></el-icon>新建厂商
      </el-button>
      <el-button
          type="warning"
          @click="handleResetProviderQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetProviderList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--模型厂商列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 165px)"
          :data="providerList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="厂商名称" width="200" align="center">
          <template #default="scope">
            <div class="provider-name-cell">
              <el-image
                  v-if="scope.row.icon"
                  :src="scope.row.icon"
                  fit="cover"
                  class="provider-icon"
              >
                <template #error>
                  <div class="provider-icon-fallback">{{ scope.row.name?.charAt(0) }}</div>
                </template>
              </el-image>
              <div v-else class="provider-icon-fallback">{{ scope.row.name?.charAt(0) }}</div>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="apiUrl" label="API地址"  width="360" align="center"/>
        <el-table-column prop="orderNum" label="排序" sortable="custom" align="center"/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="140" align="center">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="handleOpenUpdateProviderForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleModelProvider(scope.row.id)"
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
          :current-page="providerQuery.pageNo"
          :page-size="providerQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--模型厂商表单-->
    <el-drawer
        v-model="providerFormVisible"
        :title="providerFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseProviderForm"
    >
      <el-form
          :model="providerForm"
          label-width="auto"
          :rules="providerFormRules"
          ref="modelProviderFormRef"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="厂商名称" prop="name">
              <el-input
                  v-model="providerForm.name"
                  placeholder="请输入厂商名称，如：DeepSeek"
                  maxlength="50"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="厂商图标" prop="icon">
              <el-input
                  v-model="providerForm.icon"
                  placeholder="请输入厂商图标 URL 或图标标识"
                  maxlength="128"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="API地址" prop="apiUrl">
              <el-input
                  v-model="providerForm.apiUrl"
                  placeholder="请输入 API 地址，如：https://api.openai.com"
                  maxlength="128"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="密钥" prop="secretKey">
              <el-input
                  v-model="providerForm.secretKey"
                  placeholder="请输入厂商密钥（API Key），如：sk-xxxxxxxx"
                  type="password"
                  show-password
                  maxlength="128"
              />
            </el-form-item>
            <el-form-item label="厂商描述" prop="description">
              <el-input
                  v-model="providerForm.description"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入厂商描述，简要说明厂商背景与提供的服务，最多 200 字"
                  maxlength="256"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                  v-model="providerForm.remark"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入备注信息，记录其他需要说明的事项，最多 200 字"
                  maxlength="256"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="排序" prop="orderNum">
              <el-input-number
                  v-model="providerForm.orderNum"
                  :min="1" :max="999"
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
            <div>厂商名称为必填项，建议使用官方品牌名称；</div>
            <div>厂商图标可填图片 URL 或图标标识，留空时列表将不显示图标；</div>
            <div>API 地址为调用模型服务的关键配置，请确保地址可达且协议完整（如 https://api.openai.com）；</div>
            <div>密钥为必填项，系统调用厂商接口时使用，请妥善保管避免泄露；</div>
            <div>厂商描述与备注用于补充说明，便于团队协作时识别不同厂商；</div>
            <div>排序号仅填 1-100 的整数，值越小在前台列表中越靠前，相同值按创建时间排序。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button
              type="primary"
              @click="handleSubmitProviderForm"
          >保存</el-button>
          <el-button
              @click="handleCloseProviderForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageProviderListAPI, createProviderAPI, updateProviderAPI, queryProviderDetailAPI, deleteProviderAPI} from '@/api/llm/provider.js';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const providerQuery = ref({
  pageNo: 1,
  pageSize: 30,
  sorts: {},
});
const providerSearchFlag = ref({
});
const total = ref(0);
const pageSizes = [30,50,100];
const providerList = ref([]);
const providerFormVisible = ref(false);
const providerFormTitle = ref('');
const providerForm = ref({
  id: undefined,
  name: undefined,
  icon: undefined,
  apiUrl: undefined,
  secretKey: undefined,
  description: undefined,
  remark: undefined,
  orderNum: undefined,
});
const providerFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入厂商名称" }],
  icon: [{ required: true, trigger: "blur", message: "请输入厂商图标" }],
  apiUrl: [{ required: true, trigger: "blur", message: "请输入API地址" }],
  secretKey: [{ required: true, trigger: "blur", message: "请输入厂商密钥" }],
  orderNum: [{ required: true, trigger: "blur", message: "请输入排序" }],
};

handleGetProviderList();

/**
 * 删除模型厂商
 * @param id
 * */
function handleModelProvider(id) {
  ElMessageBox.confirm(
      '是否确定删除此条模型厂商?',
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
    deleteProviderAPI(data).then(res => {
      handleGetProviderList();
      ElMessage.success("删除模型厂商成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改模型厂商表单
 * @param row
 */
function handleOpenUpdateProviderForm(row) {
  const query = {
    id: row.id
  };
  queryProviderDetailAPI(query).then(res => {
    providerForm.value.id = res.data.id;
    providerForm.value.name = res.data.name;
    providerForm.value.icon = res.data.icon;
    providerForm.value.apiUrl = res.data.apiUrl;
    providerForm.value.secretKey = res.data.secretKey;
    providerForm.value.description = res.data.description;
    providerForm.value.remark = res.data.remark;
    providerForm.value.orderNum = res.data.orderNum;
    providerFormTitle.value = "修改厂商";
    providerFormVisible.value = true;
  });
}

/**
 * 打开创建模型厂商表单
 * */
function handleOpenCreateProviderForm() {
  providerForm.value.id = undefined;
  providerForm.value.name = undefined;
  providerForm.value.icon = undefined;
  providerForm.value.apiUrl = undefined;
  providerForm.value.secretKey = undefined;
  providerForm.value.description = undefined;
  providerForm.value.remark = undefined;
  providerForm.value.orderNum = 999;
  providerFormTitle.value = "创建厂商";
  providerFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetProviderQuery() {
  providerQuery.value.pageNo = 1;
  providerQuery.value.pageSize = 15;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  providerQuery.value.sorts = {};
  handleGetProviderList();
}

/**
 * 提交模型厂商表单
 * */
function handleSubmitProviderForm() {
  proxy.$refs.modelProviderFormRef.validate(valid => {
    if (valid) {
      if (!providerForm.value.id) {
        const data = {
          name: providerForm.value.name,
          icon: providerForm.value.icon,
          apiUrl: providerForm.value.apiUrl,
          secretKey: providerForm.value.secretKey,
          description: providerForm.value.description,
          remark: providerForm.value.remark,
          orderNum: providerForm.value.orderNum,
        };
        createProviderAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("模型厂商创建成功");
          handleCloseProviderForm();
          handleGetProviderList();
        })
      } else {
        const data = {
          id:  providerForm.value.id,
          name: providerForm.value.name,
          icon: providerForm.value.icon,
          apiUrl: providerForm.value.apiUrl,
          secretKey: providerForm.value.secretKey,
          description: providerForm.value.description,
          remark: providerForm.value.remark,
          orderNum: providerForm.value.orderNum,
        };
        updateProviderAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("模型厂商修改成功");
          handleCloseProviderForm();
          handleGetProviderList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseProviderForm() {
  providerForm.value.id = undefined;
  providerForm.value.name = undefined;
  providerForm.value.icon = undefined;
  providerForm.value.apiUrl = undefined;
  providerForm.value.secretKey = undefined;
  providerForm.value.description = undefined;
  providerForm.value.remark = undefined;
  providerForm.value.orderNum = undefined;
  providerFormTitle.value = "";
  providerFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetProviderList() {
  pageProviderListAPI(providerQuery.value).then(res => {
    providerList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  providerQuery.value.pageSize = pageSize;
  handleGetProviderList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  providerQuery.value.pageNo = pageNo;
  handleGetProviderList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = providerQuery.value.sorts[property];
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
    providerQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    providerQuery.value.sorts[prop] = 'desc';
  } else {
    providerQuery.value.sorts[prop] = null;
  }
  handleGetProviderList();
}
</script>

<style scoped lang="scss">
.provider-name-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.provider-icon {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  flex-shrink: 0;
}

.provider-icon-fallback {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 400;
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  flex-shrink: 0;
}

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
