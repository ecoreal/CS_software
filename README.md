# CS Software

软件构造课程作品集与综合项目整理仓库。

本仓库目前包含软件构造基础课程作业、测试数据库示例，以及从 `git@github.com:chantouRichard/BenGe.git` 整理导入的「本格视界」全栈项目。仓库定位是课程实践、项目复盘和开源分享，方便读者按目录浏览、运行和二次学习。

## 项目内容

| 目录 | 说明 |
| --- | --- |
| `assignment1` - `assignment8` | 软件构造基础课程作业，每个目录下保留对应 Solution 项目。 |
| `Test_DB` | 测试数据库相关内容。 |
| `BenGe` | 本格视界 AI 剧本创作平台，包含前端和后端两个子项目。 |

## 重点项目

### BenGe 本格视界

BenGe 是一个面向剧本创作的 AI 辅助平台，支持单人创作与多人协作，覆盖创作方向生成、剧本框架搭建、实时协同编辑、剧本分析和可视化元素管理等流程。

BenGe 已按源仓库分支整理为两个独立目录：

| 目录 | 来源分支 | 技术栈 | 说明 |
| --- | --- | --- | --- |
| `BenGe/frontend` | `frontend` | Vue 3, Pinia, Vue Router, Element Plus, Naive UI, Yjs | 前端交互界面与协同编辑客户端。 |
| `BenGe/backend` | `backend` | Spring Boot 3, Maven, MyBatis, MySQL, Redis, WebSocket | 后端 API、鉴权、房间协作、AI 服务和数据访问。 |

详细说明见 [BenGe/README.md](BenGe/README.md)。

## 快速开始

克隆仓库：

```bash
git clone git@github.com:ecoreal/CS_software.git
cd CS_software
```

运行 BenGe 前端：

```bash
cd BenGe/frontend
npm install
npm run serve
```

运行 BenGe 后端：

```bash
cd BenGe/backend
mvn spring-boot:run
```

后端默认端口为 `7122`，前端开发服务默认端口为 `8080`。完整配置和依赖说明见 [BenGe/backend/README.md](BenGe/backend/README.md) 与 [BenGe/frontend/README.md](BenGe/frontend/README.md)。

## 文档导航

- [BenGe 项目说明](BenGe/README.md)
- [BenGe 前端说明](BenGe/frontend/README.md)
- [BenGe 后端说明](BenGe/backend/README.md)
- [贡献指南](CONTRIBUTING.md)
- [安全说明](SECURITY.md)
- [变更记录](CHANGELOG.md)

## 安全与配置

后端配置文件中的数据库密码、JWT 密钥、第三方 AI 服务密钥和 ngrok token 已在本仓库中替换为环境变量占位符。运行项目前请通过本地环境变量或部署平台密钥管理功能注入真实值，避免将密钥提交到仓库。

## 贡献

欢迎围绕文档补充、运行说明、课程作业整理、BenGe 功能修复和工程化改进提交 Pull Request。提交前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 开源协议

当前仓库尚未声明开源许可证。若需要允许外部复制、修改或商用，请先补充明确的 `LICENSE` 文件。
