<template>
  <!-- 个人中心弹窗 -->
  <el-dialog
    v-model="visible"
    width="780px"
    top="6vh"
    class="user-profile-dialog"
    :show-close="false"
    :before-close="closeUserInfo"
  >
    <!-- 顶部信息横幅：放在 header 插槽内，可按住横幅拖动弹窗 -->
    <template #header>
      <div class="up-head">
        <div class="up-avatar">
          <user-avatar :user-id="userInfo.id" :size="76" shape="square">{{ avatarChar }}</user-avatar>
        </div>
        <div class="up-head-txt">
          <div class="up-name-row">
            <b>{{ userInfo.name || '用户' }}</b>
            <span class="up-badge up-badge--role">{{ roleLabel }}</span>
            <span class="up-badge up-badge--online"><i></i>在线</span>
          </div>
          <div class="up-sub">
            {{ userInfo.tenantName || '未分配租户' }} · {{ userInfo.deptPath || '未分配部门' }}
          </div>
        </div>
        <div class="up-close" @click="closeUserInfo">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTab" class="up-tabs">
      <!-- 个人资料 -->
      <el-tab-pane label="个人资料" name="profile">
        <div class="up-rows">
          <div class="up-row">
            <label>姓名</label>
            <div class="up-row-val">{{ userInfo.name || '-' }}</div>
          </div>
          <div class="up-row">
            <label>账号</label>
            <div class="up-row-val">{{ userInfo.loginName || '-' }}</div>
          </div>
          <div class="up-row">
            <label>所属部门</label>
            <div class="up-row-val">{{ userInfo.deptPath || '未分配部门' }}</div>
          </div>
          <div class="up-row">
            <label>性别</label>
            <div class="up-row-val">{{ userInfo.sexName || '-' }}</div>
          </div>
          <div class="up-row">
            <label>手机号</label>
            <div class="up-row-val">{{ userInfo.phone || '未绑定' }}</div>
          </div>
          <div class="up-row">
            <label>邮箱</label>
            <div class="up-row-val">{{ userInfo.email || '未绑定' }}</div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="passwordForm" label-width="80px" :rules="passwordFormRules" ref="passwordFormRef" class="up-form">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password/>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password/>
          </el-form-item>
          <el-form-item label="确认密码" prop="verifyPassword">
            <el-input v-model="passwordForm.verifyPassword" type="password" placeholder="请输入确认密码" show-password/>
          </el-form-item>
          <div class="up-form-actions">
            <el-button type="primary" @click="submitPasswordForm">保存修改</el-button>
          </div>
        </el-form>
      </el-tab-pane>

      <!-- 修改头像 -->
      <el-tab-pane label="修改头像" name="avatar">
        <div class="up-avatar-edit">
          <user-avatar :user-id="userInfo.id" :size="96" ref="avatarRef"/>
          <el-upload
            class="up-avatar-upload"
            :show-file-list="false"
            :action="userAvatarUploadUrl"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
            accept="image/png, image/jpeg"
          >
            <el-button type="primary" plain>上传新头像</el-button>
            <template #tip>
              <div class="el-upload__tip">请上传小于 500KB 的 JPG/PNG 文件</div>
            </template>
          </el-upload>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="up-foot">
        <span class="up-foot-note">账号由「{{ userInfo.tenantName || '未分配租户' }}」企业空间统一管理，敏感信息修改需管理员审核</span>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
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

// 当前选中的页签
const activeTab = ref('profile')

// 用户信息
const userInfo = ref({})

// 角色徽标（取第一个角色）
const roleLabel = computed(() => {
  const roles = String(userInfo.value.roleNames || '').split(',')
  return roles[0] || '成员'
})

// 横幅字头像：取姓名最后一个字
const avatarChar = computed(() => {
  const name = String(userInfo.value.name || '').trim()
  return name ? name.slice(-1) : '客'
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
    activeTab.value = 'profile'
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
        passwordForm.value = { oldPassword: undefined, newPassword: undefined, verifyPassword: undefined }
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
// 顶部横幅通铺到弹窗边缘，圆角由弹窗 overflow 裁出；位于 header 插槽，可按住拖动弹窗
.up-head {
  position: relative;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: 18px 20px;
  background-color: $app-primary-soft;
  overflow: hidden;
  cursor: move;
}

// 右侧圆环装饰
.up-head::before {
  content: "";
  position: absolute;
  right: -46px;
  top: -110px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  border: 36px solid rgba(255, 255, 255, 0.55);
  pointer-events: none;
}

.up-head::after {
  content: "";
  position: absolute;
  right: 118px;
  bottom: -130px;
  width: 190px;
  height: 190px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  pointer-events: none;
}

// 头像加载失败时回退姓名末字
.up-avatar {
  position: relative;
  z-index: 1;
  flex: none;
  width: 76px;
  height: 76px;
  border-radius: 20px;
  background: $ds-badge-grad;
  box-shadow:
    0 10px 22px -10px rgba(249, 104, 110, 0.55),
    inset 0 0 0 2px rgba(255, 255, 255, 0.4);
  overflow: hidden;

  :deep(.el-avatar) {
    width: 100%;
    height: 100%;
    background: transparent;
    color: $color-text-white;
    font-size: 30px;
    font-weight: 800;
    text-shadow: 0 2px 6px rgba(170, 60, 60, 0.28);
    user-select: none;
  }
}

.up-head-txt {
  position: relative;
  z-index: 1;
  flex: 1 1 auto;
  min-width: 0;
}

.up-name-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  b {
    font-size: 19px;
    font-weight: 800;
    color: $el-text-color-primary;
  }
}

.up-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;

  &--role {
    color: $ds-brand;
    background: $app-primary-light;
  }

  &--online {
    color: $ds-ok-tx;
    background: $ds-ok-bg;

    i {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: $ds-online;
    }
  }
}

.up-sub {
  margin-top: 5px;
  font-size: 12.5px;
  color: $color-text-secondary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.up-close {
  position: relative;
  z-index: 1;
  flex: none;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: $app-bg-card;
  border: 1px solid $app-border;
  color: $app-text-placeholder;
  font-size: 14px;
  cursor: pointer;
  transition: box-shadow $transition-fast, color $transition-fast;

  &:hover {
    color: $app-text-primary;
    box-shadow: 0 2px 8px rgba(23, 43, 77, 0.10);
  }
}

// 页签
.up-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 10px 20px 0;

  :deep(.el-tabs__header) {
    position: sticky;
    top: 0;
    z-index: 3;
    flex: none;
    margin: 0 0 $spacing-md;
    padding-top: 2px;
    background: $el-dialog-bg-color;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}

// 分组标题
.up-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 18px 0 10px;

  span {
    font-size: 12px;
    font-weight: 750;
    color: $color-text-secondary;
    letter-spacing: 0.02em;
  }

  &::after {
    content: "";
    flex: 1;
    height: 1px;
    background: $border-color-light;
  }
}

// 基本信息行
.up-rows {
  border: 1px solid $border-color-light;
  border-radius: 12px;
  overflow: hidden;
}

.up-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px $spacing-md;

  & + .up-row {
    border-top: 1px solid $border-color-light;
  }

  label {
    flex: none;
    width: 72px;
    font-size: 13px;
    font-weight: 600;
    color: $el-text-color-primary;
  }

  .up-row-val {
    flex: 1 1 auto;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 13.5px;
    font-weight: 400;
    color: $el-text-color-regular;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}

// 修改密码与修改头像
.up-form {
  display: flex;
  flex-direction: column;
  height: 100%;

  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  :deep(.el-form-item__label) {
    font-weight: 600;
    color: $el-text-color-primary;
  }

  .up-form-actions {
    margin-top: auto;
    padding: 14px 0 4px;
    display: flex;
    justify-content: flex-end;
  }
}

.up-avatar-edit {
  display: flex;
  align-items: center;
  gap: 20px;

  :deep(.el-avatar) {
    border-radius: 14px;
  }
}

.up-avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;

  .el-upload__tip {
    margin: 0;
    font-size: 11.5px;
  }
}

// 底部
.up-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;

  .up-foot-note {
    font-size: 12px;
    color: $color-text-secondary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// 深色下白色圆环装饰降透明度
html.dark {
  .up-head::before {
    border-color: rgba(255, 255, 255, 0.06);
  }

  .up-head::after {
    background: rgba(255, 255, 255, 0.05);
  }
}
</style>
