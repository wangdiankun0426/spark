<template>
  <div>
    <v-md-editor
        v-model="content"
        @save="handleSaveText"
        height="calc(100vh)"
    />
  </div>
</template>
<script setup>
import {noticeSaveTextAPI, noticeViewTextAPI} from '@/api/system/notice.js';
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import {ElMessage} from "element-plus";

const content = ref('');
const route = useRoute();
const id = route.params.id;

handleGetEditView();

/**
 * 获取公告内容
 */
function handleGetEditView() {
  if (!id) {
    return
  }
  const query = {
    id: id,
  }
  noticeViewTextAPI(query).then(res => {
    content.value = res.data;
  });
}

/**
 * 保存文档内容
 */
function handleSaveText() {
  const data = {
    id : id,
    content: content.value,
  }
  noticeSaveTextAPI(data).then(res => {
    if (!res.code) {
      return ;
    }
    ElMessage.success("保存成功");
  });
}
</script>
