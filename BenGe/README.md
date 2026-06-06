# BenGe 本格视界

AI 辅助的剧本创作与多人协作平台。

本目录整理自 `git@github.com:chantouRichard/BenGe.git`。源仓库的两个分支已拆分为两个子项目，便于作为完整全栈项目阅读和运行。

| 子目录 | 来源分支 | 说明 |
| --- | --- | --- |
| `frontend` | `frontend` | Vue 3 前端项目，负责登录、剧本创作流程、协作房间、可视化画布和协同编辑界面。 |
| `backend` | `backend` | Spring Boot 后端项目，负责用户认证、剧本管理、房间协作、WebSocket 通信、AI 服务和数据持久化。 |

## 项目定位

BenGe 面向剧本创作者和协作团队，提供从创意构思到完整剧本生成的工作流。项目将 AI 对话、结构化创作、多人协作、实时编辑和可视化分析整合到同一个平台中。

## 核心能力

- 单人创作模式：AI 引导创作方向、搭建剧本框架并生成完整剧本。
- 多人协作模式：房间管理、成员协作、方向投票、角色分工和实时聊天。
- 三阶段创作流程：方向选择、框架设计、剧本完善。
- 可视化创作画布：剧情、角色、线索和氛围卡片的关系组织。
- 实时协同编辑：基于 Yjs 的多人文本协同。
- 剧本管理与分析：剧本列表、搜索、排序、分析和元素提取。

## 技术架构

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3, Vue Router, Pinia, Element Plus, Naive UI, Vue Flow, Yjs |
| 后端 | Java 17, Spring Boot 3, Spring Security, MyBatis, WebSocket |
| 数据与协作 | MySQL, Redis, Redisson, Yjs WebSocket |
| AI 集成 | 讯飞星火相关接口配置，流式输出接口 |

## 本地运行

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认监听 `http://localhost:7122`。运行前需要准备 MySQL、Redis 和 AI 服务相关环境变量，详见 [backend/README.md](backend/README.md)。

### 2. 启动前端

```bash
cd frontend
npm install
npm run serve
```

前端开发服务默认监听 `http://localhost:8080`。详见 [frontend/README.md](frontend/README.md)。

### 3. 启动 Yjs 协同服务

```bash
cd frontend
node start-y-websocket.js
```

默认端口为 `1234`，用于第三阶段多人协同编辑。

## 目录结构

```text
BenGe/
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
└── frontend/
    ├── package.json
    ├── public/
    └── src/
```

## 配置说明

后端配置位于 `backend/src/main/resources/application.yaml`。本仓库中的敏感值已替换为环境变量占位符，例如 `DB_PASSWORD`、`JWT_SECRET`、`XFYUN_API_PASSWORD` 和 `NGROK_AUTH_TOKEN`。部署或本地运行时请通过环境变量注入真实值。

## 开发建议

- 前后端可以独立启动，联调时保持后端 `7122`、前端 `8080`、Yjs 服务 `1234` 可访问。
- 多人协作能力依赖 Redis、后端 WebSocket 和前端 Yjs WebSocket 服务。
- 修改 API、数据结构或协作消息格式时，请同步更新前后端 README 与 `frontend/docs/DataFormats.md`。

## 相关文档

- [前端说明](frontend/README.md)
- [后端说明](backend/README.md)
- [数据格式说明](frontend/docs/DataFormats.md)
