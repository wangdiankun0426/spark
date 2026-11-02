<template>
  <view class="safe-area-page" :class="themeClass">
    <!-- 顶部标题栏 -->
    <view class="page-header" :style="navHeaderStyle()">
      <view class="page-header-back" @click="onClickLeft">
        <up-icon name="arrow-left" size="19" :color="backIconColor"></up-icon>
      </view>
      <text class="page-header-title">修改头像</text>
      <view class="page-header-extra"></view>
    </view>

    <view class="update-avatar-form">
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
    </view>
  </view>
</template>
<script setup>
import {uploadAvatarAPI} from "@/api/sys/user";
import {computed} from "vue";
import {useStore} from "vuex";
import {toast} from "uview-plus";
import {navHeaderStyle} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import {getHeaderIconColor} from "@/utils/themeUtil";
import UserAvatar from "@/components/UserAvatar/index.vue"

const {isDark, themeClass} = useTheme();

const backIconColor = computed(() => getHeaderIconColor(isDark.value));
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
.update-avatar-form {
  padding-top: 50px;
  width: 90%;
  margin: auto;
}
</style>
