<template>
  <ChatLayout
    class="theme-manus"
    title="AI 超级智能体"
    subtitle="我是你的超级智能体，具备回答问题、自主规划、网络搜索等各种能力，帮你搞定复杂任务"
    :messages="messages"
    :streaming="streaming"
    placeholder="告诉我你需要什么帮助..."
    @send="handleSend"
    @clear="handleClear"
  >
    <template #header-icon>
      <div class="header-avatar">
        <icon-thunderbolt />
      </div>
    </template>
    <template #empty>
      <h2 class="welcome-title">⚡ 超级智能体待命中</h2>
      <p class="welcome-sub">我可以帮你：</p>
      <ul class="welcome-list">
        <li>回答各类知识问题，从科学技术到生活百科</li>
        <li>自主规划，拆解并执行复杂任务</li>
        <li>网络搜索，获取最新资讯并整理总结</li>
        <li>协助创作，生成方案、报告与文档</li>
      </ul>
      <p class="welcome-close">告诉我你需要什么帮助或者直接提出你的问题。</p>
      <p class="welcome-close">试试：告诉我今天武汉的天气吧！</p>
    </template>
  </ChatLayout>
</template>

<script>
import { Message } from '@arco-design/web-vue'
import ChatLayout from '@/components/ChatLayout.vue'
import { useChat } from '@/composables/useChat'
import { chatWithManus } from '@/api/chat'

export default {
  name: 'ManusView',
  components: { ChatLayout },
  setup() {
    // 启用打字机效果，逐字展示 AI 回复
    const { messages, streaming, send, clear } = useChat(
      (content, handlers) => chatWithManus(content, handlers),
      { typewriter: true }
    )

    const handleSend = (text) => send(text)

    const handleClear = () => {
      clear()
      Message.success('聊天记录已清除，开始新的对话吧~')
    }

    return { messages, streaming, handleSend, handleClear }
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
  --chat-welcome-bg: rgba(64, 128, 255, 0.06);
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
  color: #4080ff;
  background: #e8f0fe;
  box-shadow: 0 2px 8px rgba(64, 128, 255, 0.2);
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
  background: #4080ff;
}

.welcome-close {
  margin-top: 12px;
  font-size: 13px;
  color: #4080ff;
}
</style>
