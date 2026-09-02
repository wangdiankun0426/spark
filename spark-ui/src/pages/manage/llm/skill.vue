<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-input
          v-model="skillQuery.name"
          placeholder="请输入技能名称"
          clearable
          style="width: 240px; margin-right: 10px"
      />
      <el-button
          type="primary"
          @click="handleOpenCreateSkillForm"
      >
        <el-icon><Plus /></el-icon>新建技能
      </el-button>
      <el-button
          type="warning"
          @click="handleResetSkillQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetSkillList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--技能列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="skillList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="名称" width="200" align="center" sortable/>
        <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip/>
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
                @click="handleOpenUpdateSkillForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDeleteSkill(scope.row.id)"
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
          :current-page="skillQuery.pageNo"
          :page-size="skillQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--技能表单-->
    <el-drawer
        v-model="skillFormVisible"
        :title="skillFormTitle"
        direction="ltr"
        size="60%"
        :before-close="handleCloseSkillForm"
    >
      <el-form
          :model="skillForm"
          label-width="auto"
          :rules="skillFormRules"
          ref="skillFormRef"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input
                  v-model="skillForm.name"
                  placeholder="请输入技能名称，如：SQL编写规范"
                  maxlength="100"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input
                  v-model="skillForm.description"
                  placeholder="请输入一句话描述，用于装配目录供模型判断是否命中该技能"
                  type="textarea"
                  :rows="3"
                  maxlength="255"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="技能内容" prop="content">
              <v-md-editor
                  v-model="skillForm.content"
                  height="480px"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-switch
                  v-model="skillForm.status"
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
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>名称为必填项且全局唯一，是模型调用读取技能时的定位依据；</div>
            <div>描述建议写清该技能的适用场景与触发条件，将注入智能体装配目录用于命中判断；</div>
            <div>技能内容为 Markdown 指令正文，智能体命中该技能时会读取全文并严格按其执行；</div>
            <div>停用的技能不会被智能体装配目录收录。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button
              type="primary"
              @click="handleSubmitSkillForm"
          >保存</el-button>
          <el-button
              @click="handleCloseSkillForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageSkillListAPI, createSkillAPI, updateSkillAPI, querySkillDetailAPI, deleteSkillAPI} from '@/api/llm/skill.js';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const skillQuery = ref({
  name: undefined,
  pageNo: 1,
  pageSize: 30,
  sorts: {},
});
const total = ref(0);
const pageSizes = [30,50,100];
const skillList = ref([]);
const skillFormVisible = ref(false);
const skillFormTitle = ref('');
const skillForm = ref({
  id: undefined,
  name: undefined,
  description: undefined,
  content: undefined,
  status: 1,
});
const skillFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入技能名称" }],
  description: [{ required: true, trigger: "blur", message: "请输入技能描述" }],
  content: [{ required: true, trigger: "blur", message: "请输入技能内容" }],
};

handleGetSkillList();

/**
 * 删除技能
 * @param id
 * */
function handleDeleteSkill(id) {
  ElMessageBox.confirm(
      '是否确定删除此条技能?',
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
    deleteSkillAPI(data).then(res => {
      handleGetSkillList();
      ElMessage.success("删除技能成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改技能表单
 * @param row
 */
function handleOpenUpdateSkillForm(row) {
  const query = {
    id: row.id
  };
  querySkillDetailAPI(query).then(res => {
    skillForm.value.id = res.data.id;
    skillForm.value.name = res.data.name;
    skillForm.value.description = res.data.description;
    skillForm.value.content = res.data.content;
    skillForm.value.status = res.data.status;
    skillFormTitle.value = "修改技能";
    skillFormVisible.value = true;
  });
}

/**
 * 打开创建技能表单
 * */
function handleOpenCreateSkillForm() {
  skillForm.value.id = undefined;
  skillForm.value.name = undefined;
  skillForm.value.description = undefined;
  skillForm.value.content = undefined;
  skillForm.value.status = 1;
  skillFormTitle.value = "创建技能";
  skillFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetSkillQuery() {
  skillQuery.value.name = undefined;
  skillQuery.value.pageNo = 1;
  skillQuery.value.pageSize = 30;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  skillQuery.value.sorts = {};
  handleGetSkillList();
}

/**
 * 提交技能表单
 * */
function handleSubmitSkillForm() {
  proxy.$refs.skillFormRef.validate(valid => {
    if (valid) {
      if (!skillForm.value.id) {
        const data = {
          name: skillForm.value.name,
          description: skillForm.value.description,
          content: skillForm.value.content,
          status: skillForm.value.status,
        };
        createSkillAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("技能创建成功");
          handleCloseSkillForm();
          handleGetSkillList();
        })
      } else {
        const data = {
          id: skillForm.value.id,
          name: skillForm.value.name,
          description: skillForm.value.description,
          content: skillForm.value.content,
          status: skillForm.value.status,
        };
        updateSkillAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("技能修改成功");
          handleCloseSkillForm();
          handleGetSkillList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseSkillForm() {
  skillForm.value.id = undefined;
  skillForm.value.name = undefined;
  skillForm.value.description = undefined;
  skillForm.value.content = undefined;
  skillForm.value.status = 1;
  skillFormTitle.value = "";
  skillFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetSkillList() {
  pageSkillListAPI(skillQuery.value).then(res => {
    skillList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  skillQuery.value.pageSize = pageSize;
  handleGetSkillList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  skillQuery.value.pageNo = pageNo;
  handleGetSkillList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = skillQuery.value.sorts[property];
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
    skillQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    skillQuery.value.sorts[prop] = 'desc';
  } else {
    skillQuery.value.sorts[prop] = null;
  }
  handleGetSkillList();
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
