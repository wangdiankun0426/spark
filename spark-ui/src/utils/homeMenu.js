// 首页侧栏菜单单一事实来源，Web 图标栏与客户端常驻导航共用，新增页面只改这里
import Dashboard from '@/assets/icons/dashboard.vue';
import Chat from '@/assets/icons/chat.vue';
import AiChat from '@/assets/icons/aichat.vue';
import Agent from '@/assets/icons/agent.vue';
import Workflow from '@/assets/icons/workflow.vue';
import MyTodo from '@/assets/icons/myTodo.vue';
import FlowApplication from '@/assets/icons/flowApplication.vue';
import MyApplication from '@/assets/icons/myApplication.vue';
import MyDone from '@/assets/icons/myDone.vue';
import CopyMe from '@/assets/icons/copyMe.vue';

export const HOME_MENU_GROUPS = [
  {
    title: '通用',
    items: [
      { path: '/home/dashboard', label: '工作台', icon: Dashboard },
      { path: '/home/contact', label: '通讯录', icon: Chat }
    ]
  },
  {
    title: 'AI',
    items: [
      { path: '/home/aichat', label: 'AI对话', icon: AiChat },
      { path: '/home/agent', label: '智能体', icon: Agent },
      { path: '/home/workflow', label: '工作流', icon: Workflow },
      { path: '/home/workflow/review', label: '待审核', icon: MyTodo }
    ]
  },
  {
    title: '流程',
    items: [
      { path: '/home/flow/application', label: '流程申请', icon: FlowApplication },
      { path: '/home/flow/myApplication', label: '我的申请', icon: MyApplication },
      { path: '/home/flow/myTodo', label: '我的待办', icon: MyTodo },
      { path: '/home/flow/myDone', label: '我的已办', icon: MyDone },
      { path: '/home/flow/copyMe', label: '抄送给我', icon: CopyMe }
    ]
  }
];
