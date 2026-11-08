/**
 * 底部导航项
 * @returns {Array}
 */
export function visibleTabs() {
  // 底部导航tab项：工作台 / 通讯录 / AI应用 / 流程 / 我的
  return [
    {name: 'home', icon: 'home-fill', text: '工作台'},
    {name: 'contact', icon: 'man-add-fill', text: '通讯录'},
    // center：凸起圆钮，底栏视觉重心，AI 是平台的主入口
    {name: 'llm', icon: 'star-fill', text: 'AI应用', center: true},
    {name: 'flow', icon: 'file-text-fill', text: '流程'},
    {name: 'my', icon: 'account-fill', text: '我的'},
  ]
}
