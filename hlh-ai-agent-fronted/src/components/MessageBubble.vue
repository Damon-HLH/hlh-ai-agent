<template>
  <div class="message-row" :class="message.role">
    <div class="avatar" :class="message.role">
      <icon-user v-if="message.role === 'user'" />
      <icon-robot v-else />
    </div>
    <div class="message-main">
      <div
        class="bubble"
        :class="[message.role, { error: message.status === 'error', empty: message.status === 'empty' }]"
      >
        <span v-if="message.role === 'ai' && message.status === 'pending'" class="thinking-dots">
          <span></span><span></span><span></span>
        </span>
        <template v-else-if="message.status !== 'empty'">{{ message.content }}</template>
        <template v-else>（本轮没有返回内容，请重试）</template>
        <span v-if="message.role === 'ai' && message.status === 'streaming'" class="type-cursor">▍</span>
      </div>
      <div v-if="message.role === 'user'" class="status-line">
        <icon-loading v-if="message.status === 'sending'" spin class="status-icon sending" />
        <icon-check-circle
          v-else-if="message.status === 'sent' || message.status === 'done'"
          class="status-icon sent"
        />
        <icon-exclamation-circle v-else-if="message.status === 'error'" class="status-icon error" />
        <span v-if="statusText" class="status-text">{{ statusText }}</span>
        <span v-if="message.time" class="msg-time">{{ message.time }}</span>
      </div>
      <div v-else-if="message.time && message.status !== 'pending'" class="status-line ai">
        <span class="msg-time">{{ message.time }}</span>
      </div>
    </div>
  </div>
</template>

<script>
const STATUS_TEXT = {
  sending: '发送中',
  sent: '已发送',
  error: '发送失败'
}

export default {
  name: 'MessageBubble',
  props: {
    message: {
      type: Object,
      required: true
    }
  },
  computed: {
    statusText() {
      return STATUS_TEXT[this.message.status] || ''
    }
  }
}
</script>

<style scoped>
.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  flex: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.avatar.user {
  background: var(--chat-avatar-user, #a3aec4);
}

.avatar.ai {
  background: var(--chat-primary, #4080ff);
}

.message-main {
  display: flex;
  flex-direction: column;
  max-width: 76%;
}

.message-row.user .message-main {
  align-items: flex-end;
}

.bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  animation: bubble-in 0.3s ease;
}

.bubble.user {
  background: var(--chat-user-bubble, #e8f0fe);
  color: var(--chat-user-text, #2b3a55);
  border-bottom-right-radius: 4px;
}

.bubble.ai {
  background: var(--chat-ai-bubble, #ffffff);
  color: #3d4351;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(31, 41, 55, 0.06);
}

.bubble.error {
  border: 1px solid rgba(245, 63, 63, 0.35);
}

.bubble.empty {
  color: #8a91a3;
  font-style: italic;
}

/* AI 思考中的呼吸圆点 */
.thinking-dots {
  display: inline-flex;
  gap: 4px;
  padding: 4px 0;
}

.thinking-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--chat-primary, #4080ff);
  opacity: 0.35;
  animation: dot-breathe 1.2s infinite ease-in-out;
}

.thinking-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

/* 打字机光标 */
.type-cursor {
  display: inline-block;
  margin-left: 2px;
  color: var(--chat-primary, #4080ff);
  animation: cursor-blink 0.9s infinite;
}

.status-line {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 12px;
  color: #9aa1b2;
}

.status-line.ai {
  justify-content: flex-start;
}

.msg-time {
  color: #b6bcc9;
  margin-left: 2px;
}

.status-icon.sending {
  color: var(--chat-primary, #4080ff);
}

.status-icon.sent {
  color: #7bc48a;
}

.status-icon.error {
  color: #f56c6c;
}

@keyframes bubble-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes dot-breathe {
  0%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-2px);
  }
}

@keyframes cursor-blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

@media (max-width: 640px) {
  .message-main {
    max-width: 82%;
  }
}
</style>
