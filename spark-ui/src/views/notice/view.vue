<template>
  <div style="overflow-y: auto; height: 100vh">
    <div style="width: 60%; margin: auto">
      <v-md-preview
          :text="content"
          height="calc(100vh)"
          style="color: black"
      />
    </div>
  </div>
</template>
<script setup>
import { noticeViewTextAPI} from '@/api/manage/sys/notice.js';
import { ref } from 'vue';
import { useRoute } from 'vue-router';

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
</script>
