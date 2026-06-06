# BenGe Frontend

本格视界前端项目，基于 Vue 3 构建。

前端负责用户登录、剧本创作流程、多人协作房间、实时聊天、可视化创作画布、Yjs 协同编辑和剧本元素展示等交互体验。

## 功能概览

- 用户注册与登录，登录后通过 JWT 访问后端接口。
- 单人创作流程：方向选择、框架设计、完整剧本生成与可视化。
- 多人协作流程：房间列表、房间创建、成员协作、方向投票和角色分工。
- 可视化画布：剧情、角色、线索、氛围等内容的卡片化组织。
- 实时通信：后端 WebSocket 用于房间聊天和协作状态同步。
- 协同编辑：Yjs WebSocket 用于第三阶段多人文本协作。
- 导出能力：支持剧本内容和画布结果导出。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 框架 | Vue 3, Vue CLI |
| 路由与状态 | Vue Router, Pinia |
| UI | Element Plus, Naive UI, Font Awesome |
| 可视化 | Vue Flow, html2canvas, html-to-image |
| 编辑与文档 | Quill, Vue Quill, VMdEditor, Markdown 相关库 |
| 协同 | Yjs, y-websocket, y-quill |
| 请求 | Axios |

## 环境要求

- Node.js `>= 14.0.0`
- npm 或 Yarn
- 已启动的 BenGe 后端服务，默认地址 `http://localhost:7122`
- 如需协同编辑，需要启动 Yjs WebSocket 服务，默认端口 `1234`

## 安装与运行

```bash
npm install
npm run serve
```

默认开发服务监听 `http://localhost:8080`。

构建生产版本：

```bash
npm run build
```

代码检查：

```bash
npm run lint
```

启动 Yjs 协同服务：

```bash
node start-y-websocket.js
```

可通过环境变量覆盖 Yjs 服务监听地址：

```bash
HOST=0.0.0.0 PORT=1234 node start-y-websocket.js
```

## 接口与代理

前端请求封装位于 `src/api/request.js`，默认使用相对路径 `/api`。开发环境通常需要通过 Vue 代理或反向代理转发到后端 `http://localhost:7122`。

WebSocket 协作连接逻辑位于 `src/stores/socket.js`。如果需要本地联调或部署到新环境，请检查后端 WebSocket 地址和 Yjs WebSocket 地址是否与实际环境一致。

## 目录结构

```text
frontend/
├── api/                    # serverless 或部署相关入口
├── docs/                   # 数据格式与接口协作文档
├── public/                 # 静态入口文件
├── src/
│   ├── api/                # HTTP API 封装
│   ├── assets/             # 图片、图标和视频资源
│   ├── components/         # 通用组件与业务组件
│   ├── router/             # 路由配置
│   ├── stores/             # Pinia 状态管理
│   ├── utils/              # 导出、上下文收集等工具
│   └── view/               # 页面级视图
├── package.json
├── start-y-websocket.js
└── vue.config.js
```

## 常用脚本

| 命令 | 说明 |
| --- | --- |
| `npm run serve` | 启动开发服务。 |
| `npm run start` | 与 `serve` 一致，启动开发服务。 |
| `npm run build` | 构建生产产物。 |
| `npm run lint` | 执行 Vue CLI lint。 |
| `node start-y-websocket.js` | 启动 Yjs WebSocket 协同服务。 |

## 联调检查清单

- 后端服务已启动并可访问 `http://localhost:7122`。
- Redis 已启动，后端多人协作锁和房间状态可正常工作。
- Yjs WebSocket 服务已启动，端口默认为 `1234`。
- 登录后浏览器 LocalStorage 中存在 `token`。
- 前端代理、后端 CORS 和 WebSocket 地址指向同一联调环境。

## 相关文档

- [BenGe 总览](../README.md)
- [后端说明](../backend/README.md)
- [数据格式说明](docs/DataFormats.md)
