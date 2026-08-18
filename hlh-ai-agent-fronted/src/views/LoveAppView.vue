<template>
  <ChatLayout
    class="theme-love"
    title="AI 恋爱大师"
    :subtitle="`我是你的恋爱大师，耐心倾听你的感情心事，为你定制专属情感解决方案 · 聊天室 ${chatId.slice(0, 8)}`"
    :messages="messages"
    :streaming="streaming"
    placeholder="告诉我你的情感疑问..."
    @send="handleSend"
    @clear="handleClear"
  >
    <template #header-icon>
      <div class="header-avatar">
        <icon-heart-fill />
      </div>
    </template>
    <template #empty>
      <h2 class="welcome-title">💕 恋爱大师已就位</h2>
      <p class="welcome-sub">我可以帮你：</p>
      <ul class="welcome-list">
        <li>单身追求 —— 如何自然地靠近心动的人</li>
        <li>恋人相处 —— 让感情持续升温的小秘诀</li>
        <li>矛盾化解 —— 把争吵变成理解彼此的机会</li>
        <li>家庭关系 —— 婚后生活的经营之道</li>
      </ul>
      <p class="welcome-close">说说你的感情心事或者直接提出你的疑问吧~</p>
    </template>
  </ChatLayout>
</template>

<script>
import { ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import ChatLayout from '@/components/ChatLayout.vue'
import { useChat } from '@/composables/useChat'
import { chatWithLoveApp } from '@/api/chat'

// 生成聊天室 ID（用于后端会话记忆，清除记录时重新生成开启全新对话）
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
    const chatId = ref(genChatId())

    const { messages, streaming, send, clear } = useChat((content, handlers) =>
      chatWithLoveApp(content, chatId.value, handlers)
    )

    const handleSend = (text) => send(text)

    const handleClear = () => {
      clear()
      chatId.value = genChatId()
      Message.success('聊天记录已清除，开始新的对话吧~')
    }

    return { chatId, messages, streaming, handleSend, handleClear }
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
  --chat-welcome-bg: rgba(236, 111, 140, 0.06);
}

.header-avatar {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: 20px;
  color: #ec6f8c;
  background: #fdeaef;
  box-shadow: 0 2px 8px rgba(236, 111, 140, 0.2);
}

.welcome-title {
  font-size: 17px;
  font-weight: 600;
  color: #2b3245;
}

.welcome-sub {
  margin-top: 12px;
  font-size: 13px;
  color: #8a91a3;
}

.welcome-list {
  margin-top: 8px;
  list-style: none;
}

.welcome-list li {
  position: relative;
  padding: 4px 0 4px 18px;
  font-size: 14px;
  line-height: 1.7;
  color: #4a5164;
}

.welcome-list li::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 13px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ec6f8c;
}

.welcome-close {
  margin-top: 12px;
  font-size: 13px;
  color: #ec6f8c;
}
</style>
