<template>
  <ChatLayout
    class="theme-manus"
    title="AI 超级智能体"
    subtitle="具备规划与工具调用能力的全能助手，复杂任务也能搞定"
    :messages="messages"
    :streaming="streaming"
    placeholder="描述你的任务，智能体将规划并执行..."
    @send="handleSend"
  >
    <template #empty>
      <p>⚡ 超级智能体待命中</p>
      <p class="empty-sub">试试：帮我搜索今天的科技新闻并整理成摘要</p>
    </template>
  </ChatLayout>
</template>

<script>
import ChatLayout from '@/components/ChatLayout.vue'
import { useChat } from '@/composables/useChat'
import { chatWithManus } from '@/api/chat'

export default {
  name: 'ManusView',
  components: { ChatLayout },
  setup() {
    // 启用打字机效果，逐字展示 AI 回复
    const { messages, streaming, send } = useChat(
      (content, handlers) => chatWithManus(content, handlers),
      { typewriter: true }
    )

    const handleSend = (text) => send(text)

    return { messages, streaming, handleSend }
  }
}
</script>

<style scoped>
/* 超级智能体：专业蓝色调 */
.theme-manus {
  --chat-primary: #4080ff;
  --chat-bg: linear-gradient(180deg, #f3f7ff 0%, #f5f8fd 100%);
  --chat-user-bubble: linear-gradient(135deg, #6ba1ff 0%, #4080ff 100%);
  --chat-user-text: #ffffff;
  --chat-ai-bubble: #ffffff;
  --chat-avatar-user: #9db8e8;
}

:deep(.empty-sub) {
  margin-top: 6px;
  font-size: 12px;
  color: #c3c8d4;
}
</style>
