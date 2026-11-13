/**
 * 图标注册表 - 菜单 icon 串解析为组件，兼容自定义图标与 Element Plus 图标
 */
import * as ElIcon from '@element-plus/icons-vue'

const iconModules = import.meta.glob('@/assets/icons/**/*.vue', { eager: true })
const customIconMap = {}
Object.keys(iconModules).forEach(key => {
  const name = key.split('/').pop().replace(/\.vue$/, '')
  customIconMap[name] = iconModules[key].default
})

/**
 * 解析图标名为组件，自定义优先其次 EP，缺省回退工作台图标
 * @param name 图标名
 * @returns {Object}
 */
export function resolveIcon(name) {
  if (!name) {
    return customIconMap['dashboard']
  }
  return customIconMap[name] || ElIcon[name] || customIconMap['dashboard']
}