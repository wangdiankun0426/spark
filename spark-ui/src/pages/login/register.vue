<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form
          ref="registerRef"
          :model="registerForm"
          :rules="registerFormRules"
      >
        <el-form-item prop="loginName">
          <el-input
              clearable
              v-model="registerForm.loginName"
              type="text"
              placeholder="登录名"
              @keyup.enter="submitRegisterForm"
          >
            <template #prefix>
              <el-icon><Avatar /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              clearable
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              show-password
              @keyup.enter="submitRegisterForm"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
              clearable
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              show-password
              @keyup.enter="submitRegisterForm"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="sex">
          <el-select v-model="registerForm.sex" placeholder="请选择性别" style="width: 100%;">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item prop="validateValue">
          <div class="validate-row">
            <el-input
                clearable
                v-model="registerForm.validateValue"
                auto-complete="off"
                placeholder="验证码"
                @keyup.enter="submitRegisterForm"
            >
              <template #prefix>
                <el-icon><Validate /></el-icon>
              </template>
            </el-input>
            <img
                :src="validateImg"
                class="validate-img"
                @click="getValidateImg"
                title="点击刷新验证码"
                alt=""
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
              :loading="loading"
              type="primary"
              style="width: 100%; background-color: #004fc5;height: 40px; font-weight: bold; font-size: 18px"
              @click="submitRegisterForm"
          >注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-button type="primary" link @click="goLogin">立即登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getValidateCodeAPI, registerAPI } from '@/api/auth/login.js';
import { des } from '@/utils/encryptUtil.js';
import Validate from '@/assets/icons/validate.vue';
import { ref, getCurrentInstance } from "vue";
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const { proxy } = getCurrentInstance();

const registerForm = ref({
  loginName: undefined,
  password: undefined,
  confirmPassword: undefined,
  sex: undefined,
  validateId: undefined,
  validateValue: undefined
});

const validatePassword = (rule, value, callback) => {
  if (value === undefined || value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const registerFormRules = {
  loginName: [
    { required: true, trigger: "blur", message: "请输入登录名" },
    { min: 2, max: 20, trigger: "blur", message: "登录名长度为2-20个字符" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入密码" },
    { min: 6, max: 20, trigger: "blur", message: "密码长度为6-20个字符" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", validator: validatePassword }
  ],
  validateValue: [{ required: true, trigger: "blur", message: "请输入验证码" }]
};

const loading = ref(false);
const validateImg = ref(undefined);

/**
 * 获取验证码
 */
function getValidateImg() {
  getValidateCodeAPI().then(res => {
    registerForm.value.validateId = res.data.uuid;
    validateImg.value = 'data:image/png;base64,' + res.data.img;
  });
}

getValidateImg();

/**
 * 提交注册表单
 */
function submitRegisterForm() {
  proxy.$refs.registerRef.validate(valid => {
    if (!valid) return;
    loading.value = true;
    const data = {
      loginName: registerForm.value.loginName,
      password: des(registerForm.value.password),
      sex: registerForm.value.sex,
      validateId: registerForm.value.validateId,
      validateValue: registerForm.value.validateValue
    };
    registerAPI(data).then(res => {
      loading.value = false;
      if (res.code !== 200) {
        getValidateImg();
        return;
      }
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    }).catch(() => {
      loading.value = false;
      getValidateImg();
    });
  });
}

/**
 * 跳转登录页
 */
function goLogin() {
  router.push('/login');
}
</script>

<style scoped lang="scss">
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.register-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.validate-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.validate-img {
  width: 200px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.register-footer {
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}

.el-input {
  height: 44px;
}
</style>
