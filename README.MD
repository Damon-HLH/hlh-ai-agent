# HLH AI Agent · AI 恋爱大师智能体

> 基于 **Spring Boot 3 + Spring AI + RAG + Tool Calling + MCP** 构建的企业级 AI 智能体项目，包含「AI 恋爱大师」情感陪伴应用与「AI 超级智能体」两大核心能力。支持多轮对话、记忆持久化、RAG 知识库检索，并基于 ReAct 模式自主思考、规划并调用工具完成复杂任务。

## ✨ 项目亮点

- **多轮对话与记忆持久化**：基于 Spring AI 的 `MessageChatMemoryAdvisor` + `ChatMemory` 实现对话上下文记忆；提供基于 MySQL 的对话记忆持久化方案，保障对话历史可靠存储。
- **可插拔 Advisor 扩展**：自研日志记录、违禁词过滤、重读等可插拔`Advisor`顾问，实现多轮对话的拦截与增强能力扩展。
- **完整 RAG 管道**：基于 `RetrievalAugmentationAdvisor` 构建——文档阶段自动切分并由 `KeywordMetadataEnricher` 智能添加元信息支持多维过滤；存储阶段基于阿里云 RDS PostgreSQL + PGVector 实现语义向量持久化；查询阶段综合运用查询重写与上下文增强，知识库无匹配时以兜底话术友好引导，兼顾准确性与用户体验。
- **丰富的工具生态**：联网搜索、网页抓取、终端操作、文件读写、资源下载、图片搜索、邮件发送、PDF / HTML 生成等工具一应俱全，通过集中式工具注册中心统一管理。
- **MCP 服务集成**：自研图片搜索 MCP Server（集成 Pexels API），支持 STDIO 与 SSE 两种接入方式，并已部署上线供外部调用。
- **分层智能体架构**：参考 OpenManus 设计四层继承架构，基于模板方法模式逐层叠加能力，新领域智能体仅需继承并配置提示词与工具集即可。
- **配套 Vue 3 前端**：独立前端项目，支持 SSE 流式对话、打字机效果、消息状态指示、PDF / HTML 产物内联预览等能力。

## 🎯 核心功能

### 1. AI 恋爱大师（RAG 情感陪伴应用）

以恋爱知识知识库为底座，提供具备长期记忆的情感对话服务：

- 多轮对话上下文记忆，支持清除记录、会话隔离
- RAG 知识库检索增强回答，回答有据可依
- 违禁词拦截与请求日志记录，保障内容安全与可观测性

### 2. AI 超级智能体（Manus）

基于 ReAct 模式的自主规划智能体，能够拆解复杂目标并逐步执行：

- 自主思考 → 工具调用 → 观察结果 → 迭代推理，直至任务完成
- 典型场景：联网搜索资料与图片 → 下载资源 → 生成图文并茂的 HTML / PDF 约会计划报告
- 任务状态追踪与终止控制

### 3. 工具调用与集中注册

基于 `@Tool` 注解实现各能力工具，由 `ToolsRegistration` 注册中心统一管理，实现工具即插即用、热插拔：

| 工具 | 能力 |
| --- | --- |
| `WebSearchTool` / `GoogleWebSearchTool` | 联网搜索 |
| `ImageSearchTool` | 图片搜索 |
| `ResourceDownloadTool` | 资源下载 |
| `FileOperationTool` | 文件读写 |
| `HtmlGenerationTool` | Markdown → HTML 页面生成 |
| `PDFGenerationTool` | PDF 报告生成（内置中文字体三级回退加载策略） |
| `EmailSendingTool` | 邮件发送 |
| `TerminateTool` | 智能体任务终止 |

### 4. MCP 服务能力

- **hlh-image-search-mcp-server**（仓库内独立子项目）：集成 Pexels API 的图片搜索 MCP 服务，已通过腾讯云服务器部署上线，支持 STDIO 与 SSE 两种调用方式。
- 支持接入第三方 MCP 服务（如高德地图 amap-mcp-server），配置见 `mcp-servers.json`。

### 5. 文件资源服务

提供 `/api/files/{type}/{filename}` 文件资源接口，支持 PDF / HTML / 一般文件的列表查询、下载与浏览器内联预览（含路径安全校验与 CSP 安全头）。

## 🏗️ 智能体架构

参考 OpenManus，基于模板方法模式构建四层继承架构，各层职责单一、能力逐层叠加：

```
BaseAgent          # 抽象基类：定义智能体生命周期与状态流转
  └── ReActAgent       # ReAct 层：思考（Reasoning）与行动（Acting）循环
        └── ToolCallAgent  # 工具调用层：解析并执行工具调用、收集结果
              └── HlhManus     # 业务实现层：配置系统提示词与工具集，执行具体任务
```

新领域智能体只需继承 `ToolCallAgent` 并配置提示词与工具集即可快速接入，具备高扩展性与可维护性。

## 🛠️ 技术栈

| 分类 | 技术 |
| --- | --- |
| 基础框架 | Java 21 + Spring Boot 3.5.4 |
| AI 框架 | Spring AI 1.1.x + Spring AI Alibaba Agent Framework 1.1.2 |
| 大模型接入 | 阿里云 DashScope（通义千问）、LangChain4j |
| MCP | Spring AI MCP Client（WebFlux / SSE、STDIO） |
| 向量存储 | PGVector（阿里云 RDS PostgreSQL）+ SimpleVectorStore |
| 持久层 | MyBatis-Plus 3.5.12 + MySQL（对话记忆持久化） |
| 文档 / 生成 | iTextPDF 9.1.0（PDF）、CommonMark（Markdown → HTML）、Jsoup（网页抓取） |
| 工具库 | Hutool、Lombok、Fastjson2、Knife4j OpenAPI3 |
| 前端 | Vue 3 + Vue Router + Arco Design + Axios（SSE 流式） |

## 📂 项目结构

```
hlh-ai-agent/
├── src/main/java/com/hlh/hlhaiagent/
│   ├── agent/              # 智能体分层架构（BaseAgent/ReActAgent/ToolCallAgent/HlhManus）
│   ├── advisor/            # 自定义可插拔顾问（日志/违禁词过滤/重读）
│   ├── app/                # 应用编排（LoveApp 恋爱大师）
│   ├── chatmemory/         # MySQL 对话记忆持久化
│   ├── config/             # PGVector、跨域等配置
│   ├── controller/         # AI 对话、文件资源接口
│   ├── rag/                # RAG 管道（文档加载/关键词增强/查询重写/上下文增强）
│   ├── service/  mapper/   # 业务服务与持久层
│   ├── tools/              # 工具实现与集中注册中心
│   └── demo/               # 多种模型调用方式与 RAG 示例
├── src/main/resources/
│   ├── application*.yml    # 多环境配置（local / prod）
│   ├── mcp-servers.json    # MCP 服务器配置
│   └── document/           # RAG 知识库文档
├── hlh-ai-agent-fronted/           # Vue 3 前端项目
├── hlh-image-search-mcp-server/    # 图片搜索 MCP Server（独立子项目）
└── pom.xml
```

## 🖼️ 项目截图

> 截图文件位于 `src/main/resources/page` 目录。

<div align="center">
  <img src="src/main/resources/page/主页.png" alt="主页" width="720"/>
  <p><sub>主页：双应用入口导航</sub></p>
  <img src="src/main/resources/page/AI恋爱大师页面.png" alt="AI恋爱大师页面" width="720"/>
  <p><sub>AI 恋爱大师：情感对话入口</sub></p>
  <img src="src/main/resources/page/恋爱聊天.png" alt="恋爱聊天" width="720"/>
  <p><sub>恋爱大师多轮对话（RAG 知识增强 + 记忆持久化）</sub></p>
  <img src="src/main/resources/page/AI超级智能体页面.png" alt="AI超级智能体页面" width="720"/>
  <p><sub>AI 超级智能体（Manus）入口</sub></p>
  <img src="src/main/resources/page/agent聊天.png" alt="agent聊天" width="720"/>
  <img src="src/main/resources/page/agent聊天2.png" alt="agent聊天2" width="720"/>
  <p><sub>智能体自主规划与工具调用过程</sub></p>
  <img src="src/main/resources/page/HTML生成.png" alt="HTML生成" width="720"/>
  <p><sub>HTML 页面生成与内联预览</sub></p>
  <img src="src/main/resources/page/PDF生成.png" alt="PDF生成" width="720"/>
  <p><sub>PDF 约会计划报告生成</sub></p>
</div>

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Node.js 16+（前端）
- MySQL、PostgreSQL（启用 PGVector 扩展）
- 阿里云 DashScope API Key

### 启动后端

```bash
# 1. 克隆项目
git clone https://github.com/your-username/hlh-ai-agent.git
cd hlh-ai-agent

# 2. 配置密钥与数据库（application-local.yml）
#    - DashScope API Key
#    - MySQL（对话记忆持久化）
#    - PostgreSQL + PGVector（向量存储）

# 3. 启动服务（默认端口 8123）
mvn spring-boot:run
```

### 启动前端

```bash
cd hlh-ai-agent-fronted
npm install
npm run serve
```

### 图片搜索 MCP Server

```bash
cd hlh-image-search-mcp-server
# 配置 Pexels API Key 后打包
mvn clean package
# 主项目已通过 mcp-servers.json 以 STDIO 方式自动拉起，
# 也可独立部署为 SSE 服务供外部调用
```

## 📄 API 文档

启动后端后访问 Knife4j 接口文档：`http://localhost:8123/doc.html`
