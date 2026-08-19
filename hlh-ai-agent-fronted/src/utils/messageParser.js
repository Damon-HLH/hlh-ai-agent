/**
 * Agent 消息解析工具：
 * 1. 将 "Step N: 工具名称[xxx]结果: ..." 格式的智能体输出拆分为步骤段落与最终总结
 * 2. 提供轻量 Markdown 渲染（加粗、链接、图片），输出安全的 HTML
 * 3. 提取后端文件链接（/api/files/...），用于渲染可点击的文档查看卡片
 */

// 后端服务地址（文件链接需要跳转到后端查看）：
// 生产环境使用同源相对路径，由 nginx 反向代理转发到后端；
// 开发环境直接指向本地后端服务
const API_ORIGIN = process.env.NODE_ENV === 'production' ? '' : 'http://localhost:8123'

// Step 标记：兼容中英文冒号
const STEP_REGEX = /Step\s*(\d+)\s*[:：]\s*/g

// Markdown 文件链接：[文字](/api/files/xxx)
const FILE_LINK_REGEX = /\[([^\]]*)\]\((\/api\/files\/[^)\s]+)\)/g

// 已知工具的中文名称
const TOOL_LABELS = {
  googleSearch: '网络搜索',
  scrapeWebPage: '网页抓取',
  searchImage: '图片搜索',
  generateHtml: '生成 HTML',
  generatePdf: '生成 PDF',
  terminal: '终端执行',
  downloadFile: '下载资源',
  readFile: '读取文件',
  writeFile: '写入文件'
}

export function toolLabel(tool) {
  return TOOL_LABELS[tool] || tool
}

/**
 * 解析智能体回复内容
 * @returns {{ hasSteps: boolean, steps: Array, final: string, fileLinks: Array }}
 */
export function parseAgentContent(content) {
  const result = { hasSteps: false, steps: [], final: '', fileLinks: [] }
  if (!content) {
    return result
  }

  const markers = []
  let match
  STEP_REGEX.lastIndex = 0
  while ((match = STEP_REGEX.exec(content)) !== null) {
    markers.push({ num: match[1], start: match.index, bodyStart: match.index + match[0].length })
  }

  // 没有 Step 标记：整段作为最终内容
  if (markers.length === 0) {
    result.final = content
    result.fileLinks = extractFileLinks(content)
    return result
  }

  result.hasSteps = true
  const finalParts = []

  markers.forEach((marker, i) => {
    const end = i + 1 < markers.length ? markers[i + 1].start : content.length
    const body = content.slice(marker.bodyStart, end)

    const toolMatch = body.match(/^工具名称\[([^\]]+)\]\s*结果\s*[:：]\s*"?/)
    if (toolMatch) {
      let stepContent = body.slice(toolMatch[0].length)
      // 去掉结尾的引号，并把字面量 \n 转为真实换行
      stepContent = stepContent.replace(/"\s*$/, '').replace(/\\n/g, '\n')
      result.steps.push({ num: marker.num, tool: toolMatch[1], content: stepContent })
    } else {
      // 无工具结果的 Step 视为最终总结（如 "Step 4: 我已经为您..."）
      finalParts.push(body.trim())
    }
  })

  result.final = finalParts.join('\n\n')
  result.fileLinks = extractFileLinks(content)
  return result
}

/**
 * 提取内容中的后端文件链接，去重
 */
export function extractFileLinks(content) {
  const links = []
  const seen = new Set()
  let match
  FILE_LINK_REGEX.lastIndex = 0
  while ((match = FILE_LINK_REGEX.exec(content)) !== null) {
    const path = match[2]
    if (seen.has(path)) {
      continue
    }
    seen.add(path)
    const type = path.includes('/pdf/') ? 'pdf' : path.includes('/html/') ? 'html' : 'file'
    links.push({ label: match[1], url: resolveApiUrl(path), type })
  }
  return links
}

/**
 * 将 /api 开头的相对路径解析为后端完整地址
 */
export function resolveApiUrl(path) {
  if (/^https?:\/\//.test(path)) {
    return path
  }
  return API_ORIGIN + (path.startsWith('/') ? path : `/${path}`)
}

/**
 * 轻量 Markdown 渲染：先转义 HTML，再转换图片、链接、加粗
 */
export function renderRichHtml(text) {
  if (!text) {
    return ''
  }
  let html = escapeHtml(text)
  // 图片 ![alt](url)
  html = html.replace(/!\[[^\]]*\]\(([^)\s]+)\)/g, (raw, url) => {
    return `<img class="md-img" src="${resolveApiUrl(url)}" alt="图片" loading="lazy" />`
  })
  // 链接 [text](url)
  html = html.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (raw, label, url) => {
    return `<a class="md-link" href="${resolveApiUrl(url)}" target="_blank" rel="noopener">${label}</a>`
  })
  // 加粗 **text**
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  return html
}

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

/**
 * 文件链接的展示文案
 */
export function fileLabel(type) {
  if (type === 'html') {
    return '查看 HTML 文档'
  }
  if (type === 'pdf') {
    return '查看 PDF 文档'
  }
  return '下载文件'
}

/**
 * 从文件链接中提取文件名
 */
export function fileNameOf(url) {
  try {
    return decodeURIComponent(url.split('/').pop())
  } catch (e) {
    return url
  }
}
