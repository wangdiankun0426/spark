<template>
  <div class="safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="update-avatar-header">
      <view class="header-left" @click="onClickLeft">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
        <text class="header-title">修改头像</text>
      </view>
    </view>

    <div class="update-avatar-form">
      <up-form>
        <up-form-item
            label-width="100px"
            label="头像"
        >
          <UserAvatar type="user" :userId="userInfo.id" :name="userInfo.name" :size="80"></UserAvatar>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="新头像"
        >
          <up-upload
              useBeforeRead
              @beforeRead="beforeRead"
              @afterRead="uploadAvatar"
              accept="image"
          ></up-upload>
        </up-form-item>
      </up-form>
    </div>
  </div>
</template>
<script setup>
import {uploadAvatarAPI} from "@/api/sys/user";
import {computed} from "vue";
import {useStore} from "vuex";
import {toast} from "uview-plus";
import UserAvatar from "@/components/UserAvatar/index.vue"

const store = useStore();
const userInfo = computed(() => store.getters["user/getUserInfo"] || {});

/**
 * 返回
 */
function onClickLeft() {
  uni.navigateBack();
}

/**
 * 上传头像
 * @param file
 */
function uploadAvatar(file) {
  // 获取文件路径
  let filePath = '';
  // #ifdef H5
  filePath = file.file.url || file.file.path;
  // #endif
  // #ifdef MP-WEIXIN
  filePath = file.file.path;
  // #endif

  uploadAvatarAPI(filePath).then(res => {
    if (res.code !== 200) {
      return;
    }
    toast("头像修改成功");
  }).catch(err => {
    toast(err.message || "上传失败");
  });
}

function beforeRead(file) {
  // #ifdef H5
  const type = file.file.type;
  if (type !== 'image/png' && type !== 'image/jpeg') {
    toast("请上传PNG/JPG格式的图片");
    return false;
  }
  // #endif
  return true;
}
</script>
<style scoped lang="scss">
.update-avatar-header {
  flex-shrink: 0;
  padding: 16px 16px;
  background-color: #0052cc;
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}
.update-avatar-form {
  padding-top: 50px;
  width: 90%;
  margin: auto;
}
</style>
