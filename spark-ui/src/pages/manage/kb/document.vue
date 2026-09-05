<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="warning"
          @click="handleResetDocumentQuery">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetDocumentList">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--文档列表-->
    <div style="width: 100%;">
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="documentList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="名称"  min-width="300px" align="left">
          <template #default="scope">
            <DocumentIcon :ext="scope.row.ext" />
            <el-link
                class="document-name-link"
                :underline="false"
                @click="handlePreviewDocument(scope.row)"
            >
              {{ scope.row.name }}
            </el-link>
          </template>
          <template #header>
            名称
            <el-popover
                :visible="documentSearchFlag.name"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="documentSearchFlag.name ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="documentSearchFlag.name = !documentSearchFlag.name"
                />
              </template>
              <div>
                <el-input
                    v-model="documentQuery.name"
                    placeholder="请输入名称"
                    clearable
                    @input="handleGetDocumentList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="sizeStr" label="大小" align="center"/>
        <el-table-column prop="kbName" label="所属知识库" align="center" />
        <el-table-column prop="ownerName" label="所有者" align="center" />
        <el-table-column prop="createdDt" label="创建时间" align="center"/>
        <el-table-column fixed="right" label="操作" width="360" align="center">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="handleDocumentEvent(scope.row.id)"
            >
              <el-icon><HelpFilled /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               事件
              </span>
            </el-button>
            <el-button
                type="primary"
                text
                @click="handleOpenChunkPage(scope.row.id)"
            >
              <el-icon><Grid /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               分块
              </span>
            </el-button>
            <el-button
                type="info"
                text
                @click="handleDocumentMetadata(scope.row.id)"
            >
              <el-icon><HelpFilled /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               元数据
              </span>
            </el-button>
            <el-button
                type="success"
                text
                @click="handleOpenUpdateDocumentForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDeleteDocument(scope.row.id)"
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
          :current-page="documentQuery.pageNo"
          :page-size="documentQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>

    <!--文档表单-->
    <el-drawer
        v-model="documentFormVisible"
        :title="documentFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseDocumentForm"
    >
      <el-form
          :model="documentForm"
          label-width="auto"
          :rules="documentFormRules"
          ref="documentFormRef"
      >
        <el-form-item label="名称" prop="name">
          <el-input
              v-model="documentForm.name"
              placeholder="请输入名称"
          />
        </el-form-item>
        <el-form-item label="大小" prop="sizeStr">
          <el-input
              v-model="documentForm.sizeStr"
              placeholder="请输入大小"
              readonly
          />
        </el-form-item>
        <el-form-item label="所有者" prop="ownerName">
          <el-input
              v-model="documentForm.ownerName"
              placeholder="请输入所有者"
              readonly
          />
        </el-form-item>
      </el-form>
      <div class="drawer-tips">
        提示：修改文档名称不会影响文件本身，但会影响检索关键词匹配，请谨慎操作。
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button
              type="primary"
              @click="handleSubmitDocumentForm"
          >保存</el-button>
          <el-button
              @click="handleCloseDocumentForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>

    <!--文件事件详情-->
    <document-event v-model="documentEventVisible" :doc-id="documentEventDocId" />

    <!--元数据表单列表-->
    <el-drawer
        v-model="metadataVisible"
        title="元数据表单列表"
        direction="ltr"
        size="20%"
    >
      <el-table
          height="calc(100vh - 130px)"
          :data="metadataList"
      >
        <el-table-column prop="name" label="名称" align="center"/>
        <el-table-column fixed="right" label="" width="80" align="center">
          <template #default="scope">
            <el-tooltip content="选择元数据" placement="bottom">
              <el-button
                  type="primary"
                  circle
                  @click="handleOpenMetadataForm(scope.row.id)"
              >
                <el-icon><EditPen /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <!--分页组件-->
      <div>
        <el-pagination
            small
            :current-page="metadataQuery.pageNo"
            :page-size="metadataQuery.pageSize"
            :page-sizes="pageMetadataSizes"
            :background="true"
            layout="total, sizes, prev, pager, next"
            :total="metadataTotal"
            @size-change="handleMetadataPageChangeSize"
            @current-change="handleMetadataPageChangeNo"
        />
      </div>
    </el-drawer>

    <!--元数据表单-->
    <el-drawer
        v-model="metadataFormVisible"
        title="元数据表单"
        direction="ltr"
        size="40%"
    >
      <form-view
          :form="formJson"
          v-if="metadataFormVisible"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button
              type="primary"
              @click="handleSubmitMetadataForm"
          >保存</el-button>
          <el-button
              @click="metadataFormVisible = false"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="document">
import {getCurrentInstance, ref} from 'vue';
import {pageDocumentListAPI, updateDocumentAPI, queryDocumentDetailAPI, deleteDocumentAPI} from '@/api/dms/document.js';
import FormView from '@/components/FormView';
import DocumentIcon from '@/components/DocumentIcon';
import DocumentEvent from '@/components/DocumentEvent';
import { useRouter } from 'vue-router';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';
import {pageFormListAPI, queryFormJsonAPI} from "@/api/form/form.js";
import {detailFormValueAPI, saveFormValueAPI} from "@/api/form/formValue.js";

const { proxy } = getCurrentInstance();
const documentQuery = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  sorts: {},
});
const documentSearchFlag = ref({
  name: false,
});
const total = ref(0);
const pageSizes = [30,50,100];
const documentList = ref([]);
const documentFormVisible = ref(false);
const documentFormTitle = ref('');
const documentForm = ref({
  id: undefined,
  name: undefined,
  sizeStr: undefined,
  ownerName: undefined,
});
const documentEventVisible = ref(false);
const documentEventDocId = ref(undefined);
const documentFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入名称" }],
  sizeStr: [{ required: true, trigger: "blur", message: "请输入大小" }],
  ownerName: [{ required: true, trigger: "blur", message: "请输入所有者" }],
};

handleGetDocumentList();

const router = useRouter();

const metadataQuery = ref({
  pageNo: 1,
  pageSize: 30,
  type: 3,
});
const metadataList = ref([]);
const pageMetadataSizes = [15,30,50];
const metadataTotal = ref(0);
const metadataVisible = ref(false);
const currentDocId = ref(undefined);
const currentFormId = ref(undefined);
const formJson = ref(undefined);
const metadataFormVisible = ref(false);

/**
 * 打开源数据表单
 * @param formId
 */
function handleOpenMetadataForm(formId) {
  const query = {
    id: formId,
  }
  queryFormJsonAPI(query).then(res => {
    if(res.data) {
      formJson.value = JSON.parse(res.data);
      currentFormId.value = formId;
      metadataFormVisible.value = true;
    }
  });
}

/**
 * 提交元数据表单
 */
function handleSubmitMetadataForm() {
  const list = JSON.parse(JSON.stringify(formJson.value)).widgetList;
  const values = [];
  list.forEach(widget => {
    const config = widget.config;
    const value = {
      code: config.code,
      type: widget.type,
      value: config.value,
      showValue: config.showValue,
    };
    values.push(value);
  });
  const data = {
    objId: currentDocId.value,
    formId: currentFormId.value,
    values: values,
  };
  saveFormValueAPI(data).then(res => {
    if (res.code !== 200) {
      return ;
    }
    ElMessage.success("元数据提交成功");
    formJson.value = undefined;
    currentFormId.value = undefined;
    metadataFormVisible.value = false;
  });
}

/**
 * 提交文档表单
 * */
function handleSubmitDocumentForm() {
  proxy.$refs.documentFormRef.validate(valid => {
    if (valid) {
      if (!documentForm.value.id) {
         ElMessage.success("文档id不存在");
      } else {
        const data = {
          id:  documentForm.value.id,
          name: documentForm.value.name,
        };
        updateDocumentAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("文档修改成功");
          handleCloseDocumentForm();
          handleGetDocumentList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseDocumentForm() {
  documentForm.value.id = undefined;
  documentForm.value.name = undefined;
  documentForm.value.size = undefined;
  documentForm.value.path = undefined;
  documentForm.value.ext = undefined;
  documentFormTitle.value = "";
  documentFormVisible.value = false;
}

/**
 * 删除文档
 * @param id
 * */
function handleDeleteDocument(id) {
  ElMessageBox.confirm(
      '是否确定删除此条文档?',
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
    deleteDocumentAPI(data).then(res => {
      handleGetDocumentList();
      ElMessage.success("删除文档成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改文档表单
 * @param row
 */
function handleOpenUpdateDocumentForm(row) {
  const query = {
    id: row.id
  };
  queryDocumentDetailAPI(query).then(res => {
    documentForm.value.id = res.data.id;
    documentForm.value.name = res.data.name;
    documentForm.value.sizeStr = res.data.sizeStr;
    documentForm.value.ownerName = res.data.ownerName;
    documentFormTitle.value = "修改文档";
    documentFormVisible.value = true;
  });
}

/**
 * 展示文档事件
 * @param docId
 */
function handleDocumentEvent(docId) {
  documentEventDocId.value = docId;
  documentEventVisible.value = true;
}

/**
 * 展示文档元数据列表
 * @param docId
 */
function handleDocumentMetadata(docId) {
  const query = {
    objId: docId,
  }
  detailFormValueAPI(query).then(res => {
    if (res.code !== 200) {
      return ;
    }
    currentDocId.value = docId;
    if (res.data === undefined) {
      handleGetMetadataList(docId);
    } else {
      const values = res.data.values;
      if (values.length === 0) {
        handleGetMetadataList(docId);
        return ;
      }
      // 给模板中的字段赋值
      const formJsonResult = JSON.parse(res.data.formJson);
      const widgetList = formJsonResult.widgetList;
      const codes = values.map(value => value.code);
      widgetList.forEach(widget => {
        const index = codes.indexOf(widget.config.code);
        if (index === -1) {
          return;
        }
        const value = values[index].value;
        const showValue = values[index].showValue;
        widget.config.value = value;
        widget.config.showValue = showValue;
      })
      formJson.value = formJsonResult;
      currentFormId.value = res.data.formId;
      metadataFormVisible.value = true;
    }
  });
}

/**
 * 查询元数据列表
 */
function handleGetMetadataList(docId) {
  pageFormListAPI(metadataQuery.value).then(res => {
    metadataList.value = res.data.rows;
    metadataTotal.value = res.data.total;
    metadataVisible.value = true;
  })
}

/**
 * 重置查询条件
 * */
function handleResetDocumentQuery() {
  documentQuery.value.pageNo = 1;
  documentQuery.value.pageSize = 15;
  documentQuery.value.name = undefined;
  documentSearchFlag.value.name = false;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  documentQuery.value.sorts = {};
  handleGetDocumentList();
}

/**
 * 查询列表
 */
function handleGetDocumentList() {
  pageDocumentListAPI(documentQuery.value).then(res => {
    documentList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  documentQuery.value.pageSize = pageSize;
  handleGetDocumentList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  documentQuery.value.pageNo = pageNo;
  handleGetDocumentList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = documentQuery.value.sorts[property];
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
    documentQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    documentQuery.value.sorts[prop] = 'desc';
  } else {
    documentQuery.value.sorts[prop] = null;
  }
  handleGetDocumentList();
}


/**
 * 分页查询更改数量
 * @param pageSize
 */
function handleMetadataPageChangeSize(pageSize) {
  metadataQuery.value.pageSize = pageSize;

}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handleMetadataPageChangeNo(pageNo) {
  metadataQuery.value.pageNo = pageNo;

}

/**
 * 打开文档分块页（新开标签页）
 * @param docId
 */
function handleOpenChunkPage(docId) {
  const { href } = router.resolve({ path: '/document/chunk', query: { id: docId } });
  window.open(href, '_blank');
}

/**
 * 打开文档预览（新开标签页）
 * @param row
 */
function handlePreviewDocument(row) {
  const { href } = router.resolve({ path: '/document/preview', query: { id: row.id } });
  window.open(href, '_blank');
}
</script>

<style scoped lang="scss">
.document-name-link {
  color: $color-text-primary;
  transition: color $transition-fast;
  &:hover {
    color: $color-primary;
  }
}
.drawer-tips {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background-color: $color-primary-light;
  border-left: 3px solid $color-primary;
  border-radius: $border-radius-sm;
  color: $color-text-secondary;
  font-size: 12px;
  line-height: 1.6;
}
</style>
