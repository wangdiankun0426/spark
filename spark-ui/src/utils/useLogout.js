/**
 * 退出登录，Web 导航栏与客户端顶栏、账号卡三个入口共用
 */
import {ElMessageBox} from 'element-plus';
import {useRouter} from 'vue-router';
import {useStore} from 'vuex';
import {logoutAPI} from '@/api/manage/auth/login.js';

export function useLogout() {
  const router = useRouter();
  const store = useStore();

  function handleLogout() {
    ElMessageBox.confirm(
        '是否确定退出系统?',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    ).then(() => {
      logoutAPI().then(() => {
        store.dispatch('user/logout');
        router.push({path: '/login'}).catch(() => {});
      });
    }).catch(() => {});
  }

  return {handleLogout};
}
