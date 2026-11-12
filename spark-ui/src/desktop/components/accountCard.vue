<!--
  侧栏底部账号卡，替代 Web 顶栏用户信息条；
  租户切换统一收在顶栏右上角，卡内不再重复一份
-->
<template>
  <div class="dsk-account-card">
    <div class="dsk-account-card__id" @click="userInfoVisible = true">
      <span class="dsk-account-card__avatar">
        <user-avatar :user-id="userInfo.id" :size="32"/>
        <span class="dsk-account-card__online"></span>
      </span>
      <span class="dsk-account-card__meta">
        <span class="dsk-account-card__name">{{ userInfo.name || '用户' }}</span>
        <span class="dsk-account-card__login">{{ userInfo.loginName || userInfo.account || '' }}</span>
      </span>
    </div>

    <user-profile v-model="userInfoVisible" @logout="handleLogout"/>
  </div>
</template>

<script setup>
import {ref, computed} from 'vue';
import {useStore} from 'vuex';
import UserAvatar from '@/components/UserAvatar';
import UserProfile from '@/components/UserProfile';
import {useLogout} from '@/utils/useLogout.js';

const store = useStore();

const {handleLogout} = useLogout();

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined });

const userInfoVisible = ref(false);
</script>

<style scoped lang="scss">
// 与 Web 顶栏用户区一致，hover 才浮出卡片
.dsk-account-card {
  flex: none;
  margin: 10px 10px 12px;
  border-radius: 14px;
  border: 1px solid transparent;
  overflow: hidden;
  user-select: none;
  transition: background-color 0.16s ease, border-color 0.16s ease, box-shadow 0.16s ease;

  &:hover {
    background: $dsk-hover;
    border-color: $dsk-line;
    box-shadow: $dsk-card-shadow;
  }
}

.dsk-account-card__id {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  cursor: pointer;
}

.dsk-account-card__avatar {
  position: relative;
  flex: none;
  display: grid;
  place-items: center;

  .el-avatar {
    background: $dsk-avatar-bg;
  }
}

.dsk-account-card__online {
  position: absolute;
  right: -1px;
  bottom: -1px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: $dsk-online;
  box-shadow: 0 0 0 2px $dsk-avatar-ring;
}

.dsk-account-card__meta {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
  flex: 1 1 auto;
  line-height: 1.3;
}

.dsk-account-card__name {
  font-size: 13px;
  font-weight: 750;
  color: $dsk-text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dsk-account-card__login {
  font-size: 10.5px;
  font-weight: 600;
  color: $dsk-text-faint;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
