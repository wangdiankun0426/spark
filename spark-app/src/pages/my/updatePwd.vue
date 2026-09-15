<template>
  <div class="safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="update-password-header">
      <view class="header-left" @click="onClickLeft">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
        <text class="header-title">修改密码</text>
      </view>
    </view>

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
          label="新密码"
      >
        <up-input v-model="passwordForm.newPassword" placeholder="请输入新密码" border="bottom"/>
      </up-form-item>
      <up-form-item
          label-width="100px"
          label="确认密码"
      >
        <up-input v-model="passwordForm.verifyPassword" placeholder="请填写确认密码" border="bottom"/>
      </up-form-item>
      <div>
        <up-button
            class="update-password-button"
            type="primary"
            @click="submitPasswordForm"
        >
          <text class="update-password-text">保 存</text>
        </up-button>
      </div>
    </up-form>
  </div>
</div>
</template>
<script setup>
import {updatePasswordAPI, getPasswordRuleAPI} from "@/api/sys/user";
import {getEncryptKeyAPI} from "@/api/auth/login.js";
import {des} from "@/utils/encryptUtil";
import {ref} from 'vue';
import {toast} from "uview-plus";

// 租户密码长度规则，接口未返回时使用默认值
const passwordRule = ref({
  minLength: 6,
  maxLength: 12
});

const passwordForm = ref({
  oldPassword: undefined,
  newPassword: undefined,
  verifyPassword: undefined,
});

/**
 * 加载租户密码长度规则
 */
function loadPasswordRule() {
  getPasswordRuleAPI().then(res => {
    if (res.code !== 200 || !res.data) {
      return;
    }
    passwordRule.value = res.data;
  });
}

loadPasswordRule();

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
 * 校验改密表单参数，不通过时提示并阻断提交
 * @returns {boolean} 是否通过校验
 */
function validatePasswordForm() {
  const {oldPassword, newPassword, verifyPassword} = passwordForm.value;
  if (!oldPassword) {
    toast('请输入原密码');
    return false;
  }
  if (!newPassword) {
    toast('请输入新密码');
    return false;
  }
  const {minLength, maxLength} = passwordRule.value;
  if (newPassword.length < minLength || newPassword.length > maxLength) {
    toast('密码长度为' + minLength + '-' + maxLength + '个字符');
    return false;
  }
  if (newPassword === oldPassword) {
    toast('新密码不能与原密码相同');
    return false;
  }
  if (!verifyPassword) {
    toast('请再次输入密码');
    return false;
  }
  if (!equalToPassword(verifyPassword)) {
    toast('两次输入密码不一致');
    return false;
  }
  return true;
}

/**
 * 提交修改密码表单
 */
async function submitPasswordForm() {
  if (!validatePasswordForm()) {
    return;
  }
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
.update-password-header {
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
.update-password-form{
  padding-top: 20px;
  width: 90%;
  margin: auto;
}
.update-password-button {
  width: 100%;
  height: 80rpx;
  border-radius: 12rpx;
  background-color: #004fc5 !important;
  background-image: linear-gradient(90deg, #004fc5 0%, #0072e0 100%);
  box-shadow: 0 12rpx 28rpx rgba(0, 79, 197, 0.28);
  margin-top: calc(100vh - 360px);
}
.update-password-text {
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: #ffffff;
}
</style>
