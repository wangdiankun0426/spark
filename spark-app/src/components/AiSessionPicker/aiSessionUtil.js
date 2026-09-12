import {ref} from "vue";

/**
 * AI会话选择
 * 点击智能体/模型先弹出会话记录
 * 首页与AI应用页共用
 * @returns 弹层状态与操作方法
 */
export function useAiSessionPicker() {
  // 弹层显隐
  const show = ref(false);
  // 弹层标题
  const title = ref("会话记录");
  // 当前点击的智能体/模型
  const target = ref({});
  // 会话类型：agent=智能体，model=模型
  const targetType = ref("agent");

  /**
   * 打开会话选择弹层
   * @param item 智能体/模型
   * @param type 会话类型：agent=智能体，model=模型
   */
  function open(item, type) {
    target.value = item;
    targetType.value = type;
    title.value = (item.name || '会话') + ' · 会话记录';
    show.value = true;
  }

  /**
   * 选择已有会话进入对话
   * @param session 会话
   */
  function handleSelect(session) {
    openChat(session.spaceId);
  }

  /**
   * 开启新对话
   */
  function handleNew() {
    openChat(null);
  }

  /**
   * 进入聊天页
   * @param spaceId 已有会话空间id，为空表示开始新对话
   */
  function openChat(spaceId) {
    uni.navigateTo({
      url: '/views/chat/index',
      success: function (res) {
        res.eventChannel.emit('setTarget', {
          target: {
            id: target.value.id,
            name: target.value.name,
            chatSpaceId: spaceId || null
          },
          targetType: targetType.value
        });
      }
    });
  }

  return {show, title, target, targetType, open, handleSelect, handleNew};
}
