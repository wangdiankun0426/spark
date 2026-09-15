<template>
  <!-- 个人中心弹窗 -->
  <el-dialog
    v-model="visible"
    title="个人中心"
    width="800"
    style="height: 500px"
    :before-close="closeUserInfo"
  >
    <el-tabs tab-position="left">
      <el-tab-pane label="基础信息">
        <el-form :model="userInfo" label-width="80px">
          <el-form-item label="头像">
            <user-avatar
                :user-id="userInfo.id"
                :size="120"
            />
          </el-form-item>
          <el-form-item label="用户名称">
            {{ userInfo.name }}（{{ userInfo.loginName }}）
          </el-form-item>
          <el-form-item label="所属部门">
            {{ userInfo.deptPath }}
          </el-form-item>
          <el-form-item label="所属角色">
            {{ userInfo.roleNames }}
          </el-form-item>
          <el-form-item label="手机号">
            {{ userInfo.phone }}
          </el-form-item>
          <el-form-item label="邮箱">
            {{ userInfo.email }}
          </el-form-item>
          <el-form-item label="性别">
            {{ userInfo.sexName }}
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码">
        <el-form :model="passwordForm" label-width="80px" :rules="passwordFormRules" ref="passwordFormRef">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password/>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password/>
          </el-form-item>
          <el-form-item label="确认密码" prop="verifyPassword">
            <el-input v-model="passwordForm.verifyPassword" type="password" placeholder="请输入确认密码" show-password/>
          </el-form-item>
        </el-form>

        <div style="float: right">
          <el-button type="primary" @click="submitPasswordForm">确定</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="修改头像">
        <el-form-item label="原头像" label-width="80px">
          <user-avatar
              :user-id="userInfo.id"
              :size="120"
              ref="avatarRef"
          />
        </el-form-item>
        <el-form-item label="新头像" label-width="80px">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :action="userAvatarUploadUrl"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
            accept="image/png, image/jpeg"
          >
            <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                请上传小于500KB的JPG/PNG文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { updatePasswordAPI } from '@/api/manage/sys/user.js'
import { getPasswordRuleAPI } from '@/api/manage/sys/tenantConfig.js'
import { getEncryptKeyAPI } from '@/api/manage/auth/login.js'
import { des } from '@/utils/encryptUtil.js'
import UserAvatar from '@/components/UserAvatar'

const props = defineProps({
  // 弹窗显隐
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const { proxy } = getCurrentInstance()

// 弹窗显隐双向绑定
const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

// 修改密码表单校验规则
const passwordFormRules = ref({
  oldPassword: [{ required: true, trigger: 'blur', message: '请输入原密码' }],
  newPassword: [
    { required: true, trigger: 'blur', message: '请输入新密码' },
    { validator: validateNewPasswordLength, trigger: 'blur' }
  ],
  verifyPassword: [
    { required: true, trigger: 'blur', message: '请输入确认密码' },
    { required: true, validator: equalToPassword, trigger: 'blur' }
  ]
})

// 租户密码长度规则
const passwordRule = ref({
  minLength: undefined,
  maxLength: undefined
})

// 修改密码表单数据
const passwordForm = ref({
  oldPassword: undefined,
  newPassword: undefined,
  verifyPassword: undefined
})

// 用户详情
const userInfo = ref({
  id: undefined,
  name: undefined,
  sexName: undefined,
  email: undefined,
  phone: undefined
})

// 头像组件引用
const avatarRef = ref(null)
// 头像上传地址
const userAvatarUploadUrl = ref(undefined)
// 头像上传请求头
const uploadHeaders = ref({
  Authorization: undefined
})

// 弹窗打开时加载用户详情
watch(() => props.modelValue, val => {
  if (val) {
    getUserDetail()
    getPasswordRule()
  }
})

/**
 * 加载用户信息
 */
function getUserDetail() {
  userInfo.value = store.getters['user/getUserInfo'] || {}
  const baseUrl = process.env.BASE_HTTP_API
  userAvatarUploadUrl.value = baseUrl + '/sys/user/upload/avatar'
  uploadHeaders.value.Authorization = store.getters['user/getToken']
}

/**
 * 加载租户密码长度规则
 */
function getPasswordRule() {
  getPasswordRuleAPI().then(res => {
    if (res.code !== 200 || !res.data) {
      return
    }
    passwordRule.value = res.data
  })
}

/**
 * 校验新密码长度是否符合租户规则
 * @param rule
 * @param value
 * @param callback
 */
function validateNewPasswordLength(rule, value, callback) {
  if (value === undefined || value === '') {
    callback()
    return
  }
  const { minLength, maxLength } = passwordRule.value
  if (minLength == null || maxLength == null) {
    callback()
    return
  }
  if (value.length < minLength || value.length > maxLength) {
    callback(new Error('密码长度为' + minLength + '-' + maxLength + '个字符'))
    return
  }
  callback()
}

/**
 * 上传头像之前校验
 * @param file
 */
function beforeAvatarUpload(file) {
  const type = file.type
  if (type !== 'image/png' && type !== 'image/jpeg') {
    ElMessage.error('请上传PNG/JPG格式的图片')
    return false
  }
  return true
}

/**
 * 上传头像成功回调
 */
function handleAvatarSuccess(res) {
  if (res.code === 200) {
    avatarRef.value?.refresh()
    ElMessage.success('头像修改成功')
  } else {
    ElMessage.error('头像修改失败')
  }
}

/**
 * 提交修改密码
 */
function submitPasswordForm() {
  proxy.$refs.passwordFormRef.validate(async valid => {
    if (valid) {
      // 申请一次性加密密钥，本次改密两个字段共用
      const keyResult = await getEncryptKeyAPI()
      if (keyResult.code !== 200) {
        return
      }
      const data = {
        oldPassword: des(passwordForm.value.oldPassword, keyResult.data.key),
        newPassword: des(passwordForm.value.newPassword, keyResult.data.key),
        keyId: keyResult.data.keyId
      }
      updatePasswordAPI(data).then(res => {
        if (res.code !== 200) {
          return
        }
        ElMessage.success('密码修改成功')
      })
    }
  })
}

/**
 * 关闭个人中心弹窗
 */
function closeUserInfo() {
  visible.value = false
}

/**
 * 校验两次密码是否一致
 * @param rule
 * @param value
 * @param callback
 */
function equalToPassword(rule, value, callback) {
  if (passwordForm.value.newPassword !== value) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
</script>

<style scoped lang="scss">
// 头像上传组件
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: $border-radius-md;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: $transition-fast;
  width: 120px;
  height: 120px;
}
.avatar-uploader :deep(.el-upload:hover) {
  border-color: $color-primary;
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: $color-text-placeholder;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
