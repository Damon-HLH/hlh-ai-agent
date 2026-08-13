# HLH AI Agent - AI超级智能体

基于Spring AI和LangChain4j构建的智能AI交互系统，支持多种大模型接入、知识库问答和工具调用能力。

## 项目简介

HLH AI Agent是一个以AI开发为核心的智能体项目，旨在提供强大的AI交互能力，包括：

- 多轮对话与上下文记忆
- 知识库问答 (RAG)
- 工具调用与智能规划
- MCP服务集成

项目集成了主流AI大模型:阿里云DashScope的通义千问，同时支持多种AI框架，使开发者能够快速构建智能应用。

## 技术栈

- **基础框架**: Java 21 + Spring Boot 3.4.5
- **AI框架**: Spring AI + LangChain4j
- **AI模型接入**:
    - 阿里云DashScope (通义千问)
    - Ollama (本地部署模型)
- **工具与库**:
    - Knife4j (API文档)
    - Hutool (工具类库)
    - Lombok (代码简化)
- **服务配置**: 支持多环境配置

## 核心功能

### 1. 多种方式调用AI大模型

- Spring AI框架集成
- LangChain4j链式调用
- 原生SDK调用
- 自定义HTTP调用

### 2. 知识库问答 (RAG)

- 支持接入本地知识库
- 支持向量数据库检索
- 提供上下文增强查询

### 3. 智能工具调用

- 文件操作
- Google搜索
- 图片搜索
- 网页抓取
- 资源下载
- PDF生成等

### 4. MCP服务能力

- 自定义MCP服务开发
- 图片搜索服务
- 支持多种调用方式

### 5. 智能体规划能力

- 基于ReAct模式的自主规划
- 多步骤任务执行
- 任务状态追踪

## 项目结构

```
hlh-ai-agent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hlh/
│   │   │           └── hlhaiagent/
│   │   │               ├── controller/    # 接口控制器
│   │   │               ├── demo/          # 示例代码
│   │   │               │   └── invoke/    # 不同调用方式示例
│   │   │               ├── config/        # 配置类
│   │   │               ├── service/       # 服务实现
│   │   │               ├── model/         # 数据模型
│   │   │               └── HlhAiAgentApplication.java  # 启动类
│   │   └── resources/
│   │       ├── application.yml            # 通用配置
│   │       └── application-local.yml      # 本地环境配置
│   └── test/                              # 测试代码
└── pom.xml                                # 项目依赖
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- IDE: IntelliJ IDEA (推荐)