<template>
  <div class="safe-area-page">
    <up-navbar
        title="修改密码"
        bgColor="#0052cc"
        titleColor="#ffffff"
        leftIconColor="#ffffff"
        @leftClick="onClickLeft"
    />
    <div class="update-password-form">
    <up-form>
      <up-form-item
          label-width="100px"
          label="原密码"
      >
        <up-input v-model="passwordForm.oldPassword" placeholder="请输入原密码" border="bottom"/>
      </up-form-item>
      <up-form-item
          label-width="100px"
          label="原密码"
      >
        <up-input v-model="passwordForm.newPassword" placeholder="请输入新密码" border="bottom"/>
      </up-form-item>
      <up-form-item
          label-width="100px"
          label="原密码"
      >
        <up-input v-model="passwordForm.verifyPassword" placeholder="请填写确认密码" border="bottom"/>
      </up-form-item>
      <div>
        <up-button type="primary" @click="submitPasswordForm">
          保存
        </up-button>
      </div>
    </up-form>
  </div>
</div>
</template>
<script setup>
import {updatePasswordAPI} from "@/api/sys/user";
import {getEncryptKeyAPI} from "@/api/auth/login.js";
import {des} from "@/utils/encryptUtil";
import {ref} from 'vue';
import {toast} from "uview-plus";

const passwordForm = ref({
  oldPassword: undefined,
  newPassword: undefined,
  verifyPassword: undefined,
});

/**
 * 返回
 */
function onClickLeft() {
  uni.navigateBack();
}

/**
 * 检查密码是否一致
 * @param value
 * @returns {boolean}
 */
function equalToPassword(value) {
  return passwordForm.value.newPassword === value;
}

/**
 * 提交修改密码表单
 */
async function submitPasswordForm() {
  // 申请一次性加密密钥，本次改密两个字段共用
  const keyResult = await getEncryptKeyAPI();
  if (keyResult.code !== 200) {
    return;
  }
  const data = {
    oldPassword: des(passwordForm.value.oldPassword, keyResult.data.key),
    newPassword: des(passwordForm.value.newPassword, keyResult.data.key),
    keyId: keyResult.data.keyId
  }
  updatePasswordAPI(data).then(res => {
    if (res.code !== 200) {
      return ;
    }
    toast("密码修改成功");
  })
}
</script>
<style scoped lang="scss">
:deep(.u-navbar__content__title) {
  color: #ffffff !important;
}
.update-password-form{
  padding-top: 50px;
  width: 90%;
  margin: auto;
}
</style>
