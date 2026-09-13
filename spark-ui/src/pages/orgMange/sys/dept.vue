<template>
  <div class="app-container">
    <el-button
        type="primary"
        @click="openCreateDeptForm(0)"
    >
      <el-icon><Plus /></el-icon>新建部门
    </el-button>
    <el-button
        type="success"
        @click="toggleExpandAll"
    >
      <el-icon><Sort /></el-icon>{{ isAllExpanded ? '折叠' : '展开' }}
    </el-button>

    <el-table
        ref="deptTableRef"
        :data="deptTree"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        highlight-current-row
        height="calc(100vh - 112px)"
    >
      <el-table-column prop="name" label="部门名称" min-width="200" align="left"/>
      <el-table-column prop="headerName" label="部门主管" width="140" align="center"/>
      <el-table-column prop="deptNum" label="部门编码" width="160" align="center"/>
      <el-table-column prop="statusName" label="状态" width="100" align="center"/>
      <el-table-column prop="orderNum" label="排序号" width="100" align="center"/>
      <el-table-column prop="createdByName" label="创建人" width="120" align="center"/>
      <el-table-column prop="createdDt" label="创建时间" width="180" align="center"/>
      <el-table-column prop="updatedByName" label="修改人" width="120" align="center"/>
      <el-table-column prop="updatedDt" label="修改时间" width="180" align="center"/>
      <el-table-column fixed="right" label="操作" width="240" align="center">
        <template #default="scope">
          <el-button
              type="primary"
              text
              @click="openCreateDeptForm(scope.row.id)"
          >
            <el-icon><Plus /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
              新建子部门
            </span>
          </el-button>
          <el-button
              type="success"
              text
              @click="openUpdateDeptForm(scope.row)"
          >
            <el-icon><Edit /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
              修改
            </span>
          </el-button>
          <el-button
              type="danger"
              text
              @click="deleteDept(scope.row.id)"
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

  <!--部门表单抽屉-->
  <el-drawer
      v-model="deptFormVisible"
      :title="deptFormTitle"
      direction="ltr"
      size="30%"
      :before-close="closeDeptForm"
      :close-on-click-modal="false"
  >
    <el-form :model="deptForm" :rules="deptFormRules" ref="deptFormRef" label-width="auto">
      <el-form-item label="部门名称" prop="name">
        <el-input v-model="deptForm.name" placeholder="请输入部门名称"/>
      </el-form-item>
      <el-form-item label="部门主管">
        <el-select
            v-model="deptForm.headerId"
            clearable
            placeholder="选择部门主管"
        >
          <el-option
              v-for="item in userList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="部门编码">
        <el-input v-model="deptForm.deptNum" placeholder="请输入部门编码"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch
            v-model="deptForm.status"
            :active-value="1"
            :inactive-value="-1"
            active-text="已启用"
            inactive-text="已停用"
            inline-prompt
        />
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="deptForm.orderNum" :min="0" controls-position="right"/>
      </el-form-item>
      <!--必填字段填写提示-->
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>部门名称为必填项；</div>
            <div>部门主管可留空，选择后作为该部门负责人；</div>
            <div>部门编码建议使用有意义的唯一编码（如组织机构代码），便于跨系统对接；</div>
            <div>状态关闭后该部门将不再启用，已关联用户不影响登录但部门可见性受限；</div>
            <div>排序号仅填非负整数，值越小在前台列表中越靠前，相同值按创建时间排序。</div>
          </div>
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button type="primary" @click="submitDeptForm">保存</el-button>
        <el-button @click="closeDeptForm">取消</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {createDeptAPI, treeDeptAPI, updateDeptAPI, deleteDeptAPI} from '@/api/manage/sys/dept.js';
import {pageUserListAPI} from '@/api/manage/sys/user.js';
import {ElMessage, ElMessageBox} from "element-plus";
import {getCurrentInstance, nextTick, ref} from 'vue';

const { proxy } = getCurrentInstance();
const deptTableRef = ref();
const deptTree = ref([]);
const isAllExpanded = ref(true);
const deptForm = ref({
  id: undefined,
  prtId: undefined,
  name: undefined,
  headerId: undefined,
  deptNum: undefined,
  status: 1,
  orderNum: 99,
});
const deptFormRules = {
  name: [
    { required: true, trigger: "blur", message: "请输入部门名称" }
  ],
};
const deptFormVisible =  ref(false);
const deptFormTitle = ref(undefined);
const userList = ref([]);

getDeptTree();
getUserList();


/**
 * 查询用户列表
 * */
function getUserList() {
  const query = {
    page: false
  };
  pageUserListAPI(query).then(res => {
    userList.value = res.data.rows;
  })
}

/**
 * 查询部门列表
 **/
function getDeptTree() {
  treeDeptAPI().then(res => {
    deptTree.value = res.data;
    nextTick(() => {
      toggleExpandRows(deptTree.value, true);
    });
  })
}

/**
 * 切换全部展开/折叠
 */
function toggleExpandAll() {
  isAllExpanded.value = !isAllExpanded.value;
  toggleExpandRows(deptTree.value, isAllExpanded.value);
}

/**
 * 递归切换所有行展开/折叠状态
 * @param rows
 * @param expanded
 */
function toggleExpandRows(rows, expanded) {
  rows.forEach(row => {
    if (row.children && row.children.length > 0) {
      deptTableRef.value.toggleRowExpansion(row, expanded);
      toggleExpandRows(row.children, expanded);
    }
  });
}

/**
 * 打开创建部门表单
 * @param id
 */
function openCreateDeptForm(id) {
  deptForm.value.name = undefined;
  deptForm.value.headerId = undefined;
  deptForm.value.deptNum = undefined;
  deptForm.value.status = 1;
  deptForm.value.orderNum = 1;
  deptForm.value.prtId = id;
  deptFormTitle.value = "创建部门";
  deptFormVisible.value = true;
}

/**
 * 打开修改部门表单
 * @param data
 */
function openUpdateDeptForm(data) {
  deptForm.value.name = data.name;
  deptForm.value.headerId = data.headerId;
  deptForm.value.deptNum = data.deptNum;
  deptForm.value.status = data.status;
  deptForm.value.orderNum = data.orderNum;
  deptForm.value.id = data.id;
  deptFormTitle.value = "修改部门";
  deptFormVisible.value = true;
}

/**
 * 提交部门表单
 */
function submitDeptForm() {
  proxy.$refs.deptFormRef.validate(valid => {
    if (valid) {
      if (!deptForm.value.id) {
        const data = {
          prtId: deptForm.value.prtId,
          name: deptForm.value.name,
          headerId: deptForm.value.headerId,
          deptNum: deptForm.value.deptNum,
          status: deptForm.value.status,
          orderNum: deptForm.value.orderNum,
        };
        createDeptAPI(data).then(res => {
          if (res.code !== 200) {
            return;
          }
          ElMessage.success("部门创建成功");
          closeDeptForm();
          getDeptTree();
        })
      } else {
        const data = {
          id: deptForm.value.id,
          name: deptForm.value.name,
          headerId: deptForm.value.headerId,
          deptNum: deptForm.value.deptNum,
          status: deptForm.value.status,
          orderNum: deptForm.value.orderNum,
        };
        updateDeptAPI(data).then(res => {
          if (res.code !== 200) {
            return;
          }
          ElMessage.success("部门修改成功");
          closeDeptForm();
          getDeptTree();
        })
      }
    }
  });
}

/**
 * 关闭部门表单抽屉
 */
function closeDeptForm() {
  deptForm.value.name = undefined;
  deptForm.value.prtId = undefined;
  deptForm.value.id = undefined;
  deptForm.value.deptNum = undefined;
  deptForm.value.status = 1;
  deptForm.value.orderNum = 0;
  deptFormVisible.value = false;
}

/**
 * 删除部门
 * @param id
 * */
function deleteDept(id) {
  ElMessageBox.confirm(
      '是否确定删除此部门?',
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
    deleteDeptAPI(data).then(res => {
      getDeptTree();
      ElMessage.success("删除成功");
    })
  })
  .catch(() => {
  })
}
</script>
<style scoped lang="scss">
.form-tip {
  font-size: 12px;
  line-height: 20px;
  color: $color-text-secondary;
  div {
    margin-bottom: 2px;
  }
}
</style>
