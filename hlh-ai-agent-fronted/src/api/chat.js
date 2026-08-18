import { API_BASE } from './request'
import { fetchSse } from './sse'

/**
 * AI 恋爱大师：SSE 流式对话
 * GET /ai/love_app/chat/sse?message=xxx&chatId=xxx
 */
export function chatWithLoveApp(message, chatId, handlers = {}) {
  const query = new URLSearchParams({ message, chatId })
  return fetchSse(`${API_BASE}/ai/love_app/chat/sse?${query.toString()}`, handlers)
}

/**
 * AI 超级智能体：SSE 对话
 * GET /ai/manus/chat?message=xxx
 */
export function chatWithManus(message, handlers = {}) {
  const query = new URLSearchParams({ message })
  return fetchSse(`${API_BASE}/ai/manus/chat?${query.toString()}`, handlers)
}
