<template>
  <div class="chat-page">
    <header class="chat-header">
      <div class="header-inner">
        <slot name="header-icon"></slot>
        <div class="header-info">
          <h1 class="header-title">{{ title }}</h1>
          <p class="header-subtitle">{{ subtitle }}</p>
        </div>
        <a-button
          class="clear-btn"
          size="small"
          :disabled="streaming || messages.length === 0"
          @click="$emit('clear')"
        >
          <template #icon>
            <icon-delete />
          </template>
          清除记录
        </a-button>
      </div>
    </header>

    <main ref="listRef" class="chat-body" @scroll="onScroll">
      <div class="chat-inner">
        <div v-if="hasMore" class="load-earlier">
          <a-button size="mini" type="text" @click="loadEarlier">加载更早的消息</a-button>
        </div>
        <div v-if="messages.length === 0" class="empty-tip">
          <slot name="empty">发条消息，开始对话吧~</slot>
        </div>
        <transition-group name="msg">
          <MessageBubble v-for="m in visibleMessages" :key="m.id" :message="m" />
        </transition-group>
      </div>
    </main>

    <transition name="fade">
      <a-button
        v-show="showBackLatest"
        class="back-latest"
        shape="round"
        size="small"
        @click="scrollToBottom(true)"
      >
        <template #icon>
          <icon-down />
        </template>
        回到最新消息
      </a-button>
    </transition>

    <footer class="chat-footer">
      <div class="input-wrap">
        <a-textarea
          v-model="inputText"
          class="chat-input"
          :placeholder="placeholder"
          :auto-size="{ minRows: 1, maxRows: 4 }"
          allow-clear
          @keydown="onKeydown"
        />
        <a-button
          class="send-btn"
          type="primary"
          shape="circle"
          :loading="streaming"
          :disabled="!inputText.trim()"
          @click="handleSend"
        >
          <template #icon>
            <icon-send />
          </template>
        </a-button>
      </div>
      <p class="footer-tip">Enter 发送 · Shift + Enter 换行</p>
    </footer>
  </div>
</template>

<script>
import { ref, computed, watch, nextTick } from 'vue'
import MessageBubble from './MessageBubble.vue'

const PAGE_SIZE = 30

export default {
  name: 'ChatLayout',
  components: { MessageBubble },
  props: {
    title: { type: String, required: true },
    subtitle: { type: String, default: '' },
    placeholder: { type: String, default: '请输入消息...' },
    messages: { type: Array, default: () => [] },
    streaming: { type: Boolean, default: false }
  },
  emits: ['send', 'clear'],
  setup(props, { emit }) {
    const inputText = ref('')
    const listRef = ref(null)
    const visibleCount = ref(PAGE_SIZE)
    const stickBottom = ref(true)
    const showBackLatest = ref(false)
    const loadingEarlier = ref(false)

    const hasMore = computed(() => props.messages.length > visibleCount.value)

    // 长对话按需渲染：只展示最近 visibleCount 条，向上滚动加载更多
    const visibleMessages = computed(() =>
      props.messages.slice(Math.max(0, props.messages.length - visibleCount.value))
    )

    const scrollToBottom = (smooth = false) => {
      const el = listRef.value
      if (!el) {
        return
      }
      el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' })
      stickBottom.value = true
      showBackLatest.value = false
    }

    const onScroll = () => {
      const el = listRef.value
      if (!el || loadingEarlier.value) {
        return
      }
      const nearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 80
      stickBottom.value = nearBottom
      showBackLatest.value = !nearBottom
      if (el.scrollTop < 60 && hasMore.value) {
        loadEarlier()
      }
    }

    const loadEarlier = async () => {
      if (loadingEarlier.value || !hasMore.value) {
        return
      }
      loadingEarlier.value = true
      const el = listRef.value
      const prevHeight = el ? el.scrollHeight : 0
      const prevTop = el ? el.scrollTop : 0
      visibleCount.value = Math.min(props.messages.length, visibleCount.value + PAGE_SIZE)
      await nextTick()
      if (el) {
        el.scrollTop = el.scrollHeight - prevHeight + prevTop
      }
      loadingEarlier.value = false
    }

    const handleSend = () => {
      const text = inputText.value.trim()
      if (!text || props.streaming) {
        return
      }
      inputText.value = ''
      emit('send', text)
      nextTick(() => scrollToBottom())
    }

    const onKeydown = (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        handleSend()
      }
    }

    // 消息更新（新增 / 流式追加）时，若用户停留在底部则自动跟随滚动
    watch(
      () => props.messages,
      () => {
        if (stickBottom.value) {
          nextTick(() => scrollToBottom())
        }
      },
      { deep: true }
    )

    return {
      inputText,
      listRef,
      visibleMessages,
      hasMore,
      showBackLatest,
      onScroll,
      loadEarlier,
      handleSend,
      onKeydown,
      scrollToBottom
    }
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: var(--chat-bg, #f7f8fa);
}

.chat-header {
  flex: none;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(150, 160, 180, 0.12);
  z-index: 2;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 860px;
  margin: 0 auto;
  padding: 14px 20px;
}

.header-info {
  flex: 1;
  min-width: 0;
}

/* 清除记录按钮：跟随主题色 */
.clear-btn.arco-button {
  flex: none;
  margin-left: 12px;
  border-radius: 8px;
  color: var(--chat-primary, #4080ff);
  border-color: var(--chat-primary, #4080ff);
  background: transparent;
}

.clear-btn.arco-button:hover:not(.arco-button-disabled) {
  color: var(--chat-primary, #4080ff);
  border-color: var(--chat-primary, #4080ff);
  background: var(--chat-welcome-bg, rgba(64, 128, 255, 0.08));
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #2b3245;
  line-height: 1.4;
}

.header-subtitle {
  font-size: 12px;
  color: #9aa1b2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.chat-inner {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 20px;
}

.load-earlier {
  text-align: center;
  margin-bottom: 12px;
}

.empty-tip {
  max-width: 560px;
  margin: 6vh auto 0;
  padding: 30px 34px;
  background: var(--chat-welcome-bg, rgba(255, 255, 255, 0.9));
  border: 1px solid rgba(150, 160, 180, 0.1);
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(31, 41, 55, 0.05);
  text-align: left;
  color: #4a5164;
  font-size: 14px;
  animation: welcome-in 0.4s ease both;
}

@keyframes welcome-in {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.back-latest {
  position: fixed;
  right: 50%;
  transform: translateX(50%);
  bottom: 130px;
  box-shadow: 0 4px 12px rgba(31, 41, 55, 0.12);
  z-index: 3;
}

.chat-footer {
  flex: none;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-top: 1px solid rgba(150, 160, 180, 0.12);
}

.input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 860px;
  margin: 0 auto;
  padding: 14px 20px 6px;
}

.chat-input {
  border-radius: 12px;
  background: #f4f5f8;
}

.send-btn {
  flex: none;
  width: 40px;
  height: 40px;
  background: var(--chat-primary, #4080ff);
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.06);
}

.footer-tip {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 20px 10px;
  font-size: 12px;
  color: #b3b9c6;
}

/* 消息进入动画 */
.msg-enter-active {
  transition: all 0.3s ease;
}

.msg-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .header-inner {
    padding: 12px 14px;
  }

  .header-title {
    font-size: 15px;
  }

  .clear-btn.arco-button :deep(span:not(.arco-icon)) {
    display: none;
  }

  .chat-inner {
    padding: 16px 14px;
  }

  .input-wrap {
    padding: 10px 14px 4px;
  }

  .back-latest {
    bottom: 118px;
  }
}
</style>
