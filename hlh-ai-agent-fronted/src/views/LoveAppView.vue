<template>
  <ChatLayout
    class="theme-love"
    title="AI 恋爱大师"
    :subtitle="`温柔懂你的恋爱顾问 · 聊天室 ID：${chatId.slice(0, 8)}`"
    :messages="messages"
    :streaming="streaming"
    placeholder="说说你的恋爱烦恼，大师为你支招..."
    @send="handleSend"
  >
    <template #empty>
      <p>💕 恋爱大师已就位</p>
      <p class="empty-sub">聊聊你喜欢的人，或者最近的感情困惑吧~</p>
    </template>
  </ChatLayout>
</template>

<script>
import ChatLayout from '@/components/ChatLayout.vue'
import { useChat } from '@/composables/useChat'
import { chatWithLoveApp } from '@/api/chat'

// 生成聊天室 ID（页面加载时自动生成，用于后端会话记忆）
const genChatId = () => {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `chat-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export default {
  name: 'LoveAppView',
  components: { ChatLayout },
  setup() {
    const chatId = genChatId()

    const { messages, streaming, send } = useChat((content, handlers) =>
      chatWithLoveApp(content, chatId, handlers)
    )

    const handleSend = (text) => send(text)

    return { chatId, messages, streaming, handleSend }
  }
}
</script>

<style scoped>
/* 恋爱大师：柔和粉色调 */
.theme-love {
  --chat-primary: #ec6f8c;
  --chat-bg: linear-gradient(180deg, #fdf4f6 0%, #faf3f8 100%);
  --chat-user-bubble: linear-gradient(135deg, #f9b8c8 0%, #f08aa4 100%);
  --chat-user-text: #ffffff;
  --chat-ai-bubble: #ffffff;
  --chat-avatar-user: #f0a3b8;
}

:deep(.empty-sub) {
  margin-top: 6px;
  font-size: 12px;
  color: #c3c8d4;
}
</style>
