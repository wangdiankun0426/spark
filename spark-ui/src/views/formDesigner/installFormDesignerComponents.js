const widgetModules = import.meta.glob('./formWidget/basicWidget/*.vue', { eager: true })
const settingModules = import.meta.glob('./settingPanel/basicWidget/*.vue', { eager: true })

/**
 * 按组件声明的名称注册表单设计器基础组件及设置组件
 * @param app Vue应用实例
 */
export function installFormDesignerComponents(app) {
    const modules = [widgetModules, settingModules]
    modules.forEach(moduleGroup => {
        Object.values(moduleGroup).forEach(module => {
            const component = module.default
            app.component(component.name, component)
        })
    })
}
