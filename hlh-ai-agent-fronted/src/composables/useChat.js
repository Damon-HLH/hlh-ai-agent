import { ref } from 'vue'
import { Message } from '@arco-design/web-vue'

let seed = 0
const genId = () => `msg-${Date.now()}-${++seed}`

// 消息时间戳，格式 HH:mm
const nowTime = () => {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

/**
 * 通用聊天逻辑组合式函数
 * @param {Function} sendFn 发送函数，接收 (content, handlers)，返回 Promise
 * @param {Object} options { typewriter: boolean } 是否启用打字机效果
 */
export function useChat(sendFn, options = {}) {
  const { typewriter = false } = options

  const messages = ref([])
  const streaming = ref(false)

  async function send(text) {
    const content = (text || '').trim()
    if (!content) {
      return
    }
    if (streaming.value) {
      Message.warning('AI 正在回复中，请稍候~')
      return
    }

    messages.value.push(
      { id: genId(), role: 'user', content, status: 'sending', time: nowTime() },
      { id: genId(), role: 'ai', content: '', status: 'pending', time: nowTime() }
    )
    // 注意：必须通过响应式数组取出代理对象再修改，直接改原始对象不会触发视图更新
    const userMsg = messages.value[messages.value.length - 2]
    const aiMsg = messages.value[messages.value.length - 1]
    streaming.value = true

    // 打字机缓冲：已接收但未展示的内容
    let buffer = ''
    let timer = null
    let streamFinished = false

    const finishOk = () => {
      aiMsg.status = aiMsg.content ? 'done' : 'empty'
      streaming.value = false
    }

    const typeTick = () => {
      if (buffer.length > 0) {
        const take = Math.min(buffer.length, 3)
        aiMsg.content += buffer.slice(0, take)
        buffer = buffer.slice(take)
      } else if (streamFinished) {
        clearInterval(timer)
        timer = null
        finishOk()
      }
    }

    const ensureTimer = () => {
      if (typewriter && !timer) {
        timer = setInterval(typeTick, 24)
      }
    }

    try {
      await sendFn(content, {
        onOpen: () => {
          userMsg.status = 'sent'
        },
        onMessage: (chunk) => {
          if (userMsg.status === 'sending') {
            userMsg.status = 'sent'
          }
          if (aiMsg.status === 'pending') {
            aiMsg.status = 'streaming'
          }
          if (typewriter) {
            buffer += chunk
            ensureTimer()
          } else {
            aiMsg.content += chunk
          }
        }
      })
      streamFinished = true
      if (typewriter && buffer.length > 0) {
        ensureTimer()
      } else {
        if (timer) {
          clearInterval(timer)
          timer = null
        }
        finishOk()
      }
    } catch (error) {
      streamFinished = true
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      if (userMsg.status === 'sending') {
        userMsg.status = 'error'
      }
      aiMsg.status = 'error'
      if (!aiMsg.content) {
        aiMsg.content = ''
      }
      Message.error(error.message === 'Failed to fetch'
        ? '连接后端服务失败，请确认服务已启动'
        : `回复失败：${error.message}`)
      streaming.value = false
    }
  }

  function clear() {
    messages.value = []
    streaming.value = false
  }

  return {
    messages,
    streaming,
    send,
    clear
  }
}
