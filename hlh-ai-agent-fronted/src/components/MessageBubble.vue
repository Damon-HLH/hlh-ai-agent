<template>
  <div class="message-row" :class="message.role">
    <div class="avatar" :class="message.role">
      <icon-user v-if="message.role === 'user'" />
      <icon-robot v-else />
    </div>
    <div class="message-main">
      <!-- 智能体富渲染：步骤卡片 + 最终总结 + 文件查看卡片 -->
      <template v-if="useRich">
        <div v-if="message.status === 'pending' && !message.content" class="bubble ai">
          <span class="thinking-dots"><span></span><span></span><span></span></span>
        </div>
        <div v-else-if="message.status === 'error'" class="bubble ai error">
          {{ message.content || '回复失败，请重试' }}
        </div>
        <div v-else class="agent-message">
          <div v-if="parsed.steps.length" class="step-list">
            <div v-for="step in parsed.steps" :key="step.num" class="step-card">
              <div class="step-header" @click="toggleStep(step.num)">
                <icon-code class="step-tool-icon" />
                <span class="step-title">Step {{ step.num }} · {{ getToolLabel(step.tool) }}</span>
                <icon-loading
                  v-if="isStreaming && lastStepNum === step.num"
                  spin
                  class="step-status running"
                />
                <icon-check-circle v-else class="step-status done" />
                <icon-down class="step-arrow" :class="{ rotate: isStepOpen(step.num) }" />
              </div>
              <div
                v-show="isStepOpen(step.num)"
                class="step-body"
                v-html="renderHtml(step.content)"
              ></div>
            </div>
          </div>

          <div v-if="parsed.final || isStreaming" class="bubble ai agent-final">
            <span v-html="renderHtml(parsed.final)"></span>
            <span v-if="isStreaming" class="type-cursor">▍</span>
          </div>

          <div v-if="parsed.fileLinks.length" class="file-links">
            <a
              v-for="f in parsed.fileLinks"
              :key="f.url"
              :href="f.url"
              target="_blank"
              rel="noopener"
              class="file-card"
            >
              <span class="file-icon" :class="f.type">
                <icon-file />
              </span>
              <span class="file-info">
                <span class="file-label">{{ getFileLabel(f.type) }}</span>
                <span class="file-name">{{ getFileName(f.url) }}</span>
              </span>
              <icon-export class="file-export" />
            </a>
          </div>
        </div>
      </template>

      <!-- 普通气泡渲染 -->
      <div
        v-else
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
import { ref, computed } from 'vue'
import {
  parseAgentContent,
  renderRichHtml,
  toolLabel,
  fileLabel,
  fileNameOf
} from '@/utils/messageParser'

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
    },
    // 是否启用智能体富渲染（Step 步骤卡片 + Markdown + 文件卡片）
    rich: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    // 手动展开/收起的步骤状态，未手动操作时：流式输出中最新步骤自动展开
    const openMap = ref({})

    const useRich = computed(() => props.rich && props.message.role === 'ai')
    const parsed = computed(() =>
      useRich.value ? parseAgentContent(props.message.content) : null
    )
    const isStreaming = computed(() => props.message.status === 'streaming')
    const lastStepNum = computed(() => {
      const steps = parsed.value ? parsed.value.steps : []
      return steps.length > 0 ? steps[steps.length - 1].num : null
    })

    const isStepOpen = (num) => {
      if (openMap.value[num] !== undefined) {
        return openMap.value[num]
      }
      return isStreaming.value && num === lastStepNum.value
    }

    const toggleStep = (num) => {
      openMap.value[num] = !isStepOpen(num)
    }

    return {
      useRich,
      parsed,
      isStreaming,
      lastStepNum,
      isStepOpen,
      toggleStep,
      renderHtml: renderRichHtml,
      getToolLabel: toolLabel,
      getFileLabel: fileLabel,
      getFileName: fileNameOf
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

/* ---------- 智能体富渲染 ---------- */
.agent-message {
  display: flex;
  flex-direction: column;
  width: 100%;
  animation: bubble-in 0.3s ease;
}

.agent-final {
  margin-top: 2px;
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 8px;
}

.step-card {
  background: var(--chat-ai-bubble, #ffffff);
  border: 1px solid rgba(150, 160, 180, 0.12);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(31, 41, 55, 0.05);
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s ease;
}

.step-header:hover {
  background: rgba(150, 160, 180, 0.07);
}

.step-tool-icon {
  flex: none;
  font-size: 15px;
  color: var(--chat-primary, #4080ff);
}

.step-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  font-weight: 500;
  color: #4a5164;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.step-status.running {
  flex: none;
  color: var(--chat-primary, #4080ff);
}

.step-status.done {
  flex: none;
  color: #7bc48a;
}

.step-arrow {
  flex: none;
  font-size: 12px;
  color: #b6bcc9;
  transition: transform 0.25s ease;
}

.step-arrow.rotate {
  transform: rotate(180deg);
}

.step-body {
  padding: 10px 14px 12px;
  border-top: 1px dashed rgba(150, 160, 180, 0.2);
  font-size: 13px;
  line-height: 1.7;
  color: #5a6172;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 280px;
  overflow-y: auto;
}

/* 富文本内容（v-html 注入，需要 :deep） */
:deep(.md-img) {
  display: block;
  max-width: 100%;
  max-height: 200px;
  margin: 6px 0;
  border-radius: 8px;
  object-fit: cover;
}

:deep(.md-link) {
  color: var(--chat-primary, #4080ff);
  text-decoration: underline;
  text-underline-offset: 3px;
  word-break: break-all;
}

:deep(strong) {
  color: #2b3245;
}

/* 文件查看卡片 */
.file-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--chat-ai-bubble, #ffffff);
  border: 1px solid rgba(150, 160, 180, 0.15);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(31, 41, 55, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.file-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(31, 41, 55, 0.1);
  border-color: var(--chat-primary, #4080ff);
}

.file-icon {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  font-size: 18px;
  color: #fff;
  background: var(--chat-primary, #4080ff);
}

.file-icon.pdf {
  background: #f08080;
}

.file-icon.file {
  background: #a3aec4;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.file-label {
  font-size: 14px;
  font-weight: 600;
  color: #2b3245;
}

.file-name {
  margin-top: 2px;
  font-size: 12px;
  color: #9aa1b2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-export {
  flex: none;
  color: #b6bcc9;
}

/* ---------- 通用动画与状态 ---------- */

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
