/**
 * 操作权限指令 - 无权限时移除元素
 * 用法 v-hasPermi="['sys:user:create']"，命中任一权限串即保留
 */
import { checkPermi } from '@/utils/permissionUtil.js'

export const hasPermi = {
  mounted(el, binding) {
    const perms = binding.value
    if (!perms || !perms.length) {
      return
    }
    const allowed = perms.some(perm => checkPermi(perm))
    if (!allowed && el.parentNode) {
      el.parentNode.removeChild(el)
    }
  }
}

/**
 * 注册权限指令
 * @param app Vue 应用实例
 */
export function installPermissionDirectives(app) {
  app.directive('hasPermi', hasPermi)
}