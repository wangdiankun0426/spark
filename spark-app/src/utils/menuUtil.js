/**
 * 底部导航项
 * @returns {Array}
 */
export function visibleTabs() {
  // 底部导航tab项
  return [
    {name: 'home', icon: 'home-fill', text: '工作台'},
    {name: 'flow', icon: 'order', text: '流程'},
    {name: 'llm', icon: 'grid-fill', text: 'AI应用'},
    {name: 'message', icon: 'chat-fill', text: '消息'},
    {name: 'my', icon: 'account', text: '我的'},
  ];
}