# hlh-ai-agent-fronted


## 项目结构

hlh-ai-agent-fronted/src/
├── api/
│   ├── request.js      # axios 实例，统一错误提示
│   ├── sse.js          # fetch + ReadableStream 实现的 SSE 解析器
│   └── chat.js         # 两个后端接口的封装
├── composables/
│   └── useChat.js      # 通用聊天逻辑（消息状态机、流式接收、打字机效果）
├── components/
│   ├── ChatLayout.vue  # 可复用聊天布局（顶栏/消息区/输入区），CSS 变量换肤
│   └── MessageBubble.vue  # 消息气泡（状态图标、思考动画、打字光标）
├── views/
│   ├── HomeView.vue    # 卡片式导航主页
│   ├── LoveAppView.vue # 恋爱大师（粉色主题）
│   └── ManusView.vue   # 超级智能体（蓝色主题 + 打字机）
└── router/index.js     # 三个路由，懒加载 + 页面标题

## 核心实现要点

核心实现要点
SSE 客户端：sse.js 采用 fetch 流式解析而非原生 EventSource，兼容后端 Flux<String> 和 SseEmitter 两种返回形式，正确处理 data:` 事件切分与[DONE]结束标记。
恋爱大师页：进入页面自动生成 UUID 聊天室 ID（显示在顶部副标题），SSE 实时流式展示回复。
超级智能体页：启用打字机效果（缓冲队列 + 定时器逐字渲染）、用户消息状态指示（发送中 → 已发送/失败），消息列表按需渲染，向上滚动自动加载更早消息，滚离底部时出现"回到最新消息"按钮。
组件复用：两个聊天页共用同一套 ChatLayout + useChat，仅通过 CSS 变量和配置参数区分主题与行为。
全局体验：页面切换过渡动画、消息进入动画、悬停效果、响应式适配移动端、统一的 arco Message 错误提示。

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
