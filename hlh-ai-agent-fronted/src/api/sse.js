/**
 * 基于 fetch + ReadableStream 的 SSE 客户端。
 * 相比原生 EventSource：
 * 1. 无 URL 长度限制问题（长消息更安全）
 * 2. 可统一进行错误处理与中断控制
 */
export async function fetchSse(url, { onOpen, onMessage, signal } = {}) {
  let response
  try {
    response = await fetch(url, {
      method: 'GET',
      headers: { Accept: 'text/event-stream' },
      signal
    })
  } catch (error) {
    if (error.name === 'AbortError') {
      return
    }
    throw error
  }

  if (!response.ok) {
    throw new Error(`请求失败（${response.status}）`)
  }

  if (onOpen) {
    onOpen()
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  const handleEvent = (rawEvent) => {
    const dataLines = rawEvent
      .split('\n')
      .filter((line) => line.startsWith('data:'))
      .map((line) => line.slice(5).replace(/^ /, ''))
    if (dataLines.length === 0) {
      return
    }
    const data = dataLines.join('\n')
    // Spring 的 Flux<String> / SseEmitter 结束标记
    if (data === '[DONE]') {
      return
    }
    if (onMessage) {
      onMessage(data)
    }
  }

  // eslint-disable-next-line no-constant-condition
  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }
    // 统一换行符，避免 \r\n 影响事件切分
    buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
    let sepIndex
    while ((sepIndex = buffer.indexOf('\n\n')) !== -1) {
      const rawEvent = buffer.slice(0, sepIndex)
      buffer = buffer.slice(sepIndex + 2)
      handleEvent(rawEvent)
    }
  }

  if (buffer.trim()) {
    handleEvent(buffer)
  }
}
