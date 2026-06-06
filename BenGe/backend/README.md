# BenGe Backend

本格视界后端项目，基于 Spring Boot 3 构建。

后端负责用户认证、剧本管理、房间协作、实时通信、AI 流式生成、数据持久化和 Redis 分布式锁等能力。

## 功能概览

- 用户注册、登录和 JWT 鉴权。
- 剧本创建、查询、删除、阶段推进、分析与可视化元素管理。
- 房间创建、加入、退出、成员管理和多人协作状态同步。
- WebSocket 聊天、角色分工、投票、画布同步和协作消息路由。
- SSE 流式接口，用于 AI 生成与聊天输出。
- MyBatis 数据访问，支持 MySQL 持久化。
- Redis 与 Redisson，用于协作锁和并发控制。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 语言与框架 | Java 17, Spring Boot 3.0.12 |
| 安全 | Spring Security, JWT |
| 数据访问 | MyBatis, MySQL |
| 实时通信 | Spring WebSocket |
| 并发协作 | Redis, Redisson |
| 文档与测试 | Springdoc OpenAPI, JUnit |
| AI 调用 | 讯飞星火相关 HTTP 接口配置 |

## 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8+
- Redis 3.2+ 或兼容版本
- 可用的 AI 服务密钥，用于 `xfyun` 相关配置

## 配置

主配置文件：

```text
src/main/resources/application.yaml
```

本仓库已将敏感值改为环境变量占位符。运行前请按需设置：

| 环境变量 | 说明 |
| --- | --- |
| `DB_PASSWORD` | MySQL 数据库密码。 |
| `JWT_SECRET` | JWT 签名密钥。 |
| `XFYUN_APPID` | 讯飞应用 ID。 |
| `XFYUN_API_PASSWORD` | 讯飞接口密码。 |
| `XFYUN_APPID_WWB` | 图像或扩展接口应用 ID。 |
| `XFYUN_X1_API_PASSWORD` | X1 模型接口密码。 |
| `XFYUN_ULTRA_API_PASSWORD` | Ultra 模型接口密码。 |
| `XFYUN_GEN_IMAGE_API_SECRET_WWB` | 文生图接口 Secret。 |
| `XFYUN_GEN_IMAGE_API_KEY_WWB` | 文生图接口 Key。 |
| `XFYUN_X1_HTTP_API_PASSWORD` | X1 HTTP 流式接口密码。 |
| `NGROK_AUTH_TOKEN` | ngrok token，仅在需要内网穿透时使用。 |

默认服务端口为 `7122`，Redis 默认连接 `redis://127.0.0.1:6379`，MySQL 默认连接数据库 `picture`。

## 数据库初始化

数据库建表脚本位于：

```text
src/main/resources/sql/script_tables.sql
```

建议在首次运行前创建数据库并导入表结构：

```sql
CREATE DATABASE picture CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

随后执行 `script_tables.sql`。

## 运行

```bash
mvn spring-boot:run
```

如需显式注入环境变量：

```bash
DB_PASSWORD=your-password \
JWT_SECRET=your-jwt-secret \
mvn spring-boot:run
```

## 测试

```bash
mvn test
```

部分测试依赖 MySQL、Redis、外部 AI 服务或 WebSocket 环境。若本地未配置这些依赖，相关集成测试可能无法通过。

## 主要接口

| 模块 | 路径前缀 | 说明 |
| --- | --- | --- |
| 用户 | `/api/user` | 注册、登录和 token 返回。 |
| 剧本 | `/api/script` | 剧本创建、查询、删除、阶段生成和分析。 |
| AI | `/api/ai` | Slogan、聊天、剧本生成等流式与非流式 AI 接口。 |
| 房间 | `/api/room` | 房间创建、列表、加入、退出、投票和协作流程。 |
| WebSocket | `/ws` | 房间聊天、协作状态和画布同步。 |

## 目录结构

```text
backend/
├── pom.xml
├── src/main/java/com/bengebackend/
│   ├── config/          # 安全、Token、WebSocket、AI 配置
│   ├── controller/      # REST API 与 SSE 接口
│   ├── dto/             # 前后端传输对象
│   ├── entity/          # 请求实体与消息实体
│   ├── mapper/          # MyBatis Mapper 接口
│   ├── model/           # 数据模型
│   ├── service/         # 业务服务
│   ├── util/            # 工具类
│   └── websocket/       # WebSocket 消息处理
├── src/main/resources/
│   ├── mapper/          # MyBatis XML
│   ├── sql/             # 建表脚本
│   └── application.yaml
└── src/test/java/
```

## 安全建议

- 不要提交真实数据库密码、JWT secret、AI key 或 ngrok token。
- 生产环境请使用强随机 `JWT_SECRET`。
- 生产环境不要使用默认数据库账户密码。
- 对外部署时请限制 CORS 来源，并使用 HTTPS/WSS。
- AI 服务和 WebSocket 日志中可能包含用户创作内容，部署时应按数据隐私要求控制日志级别。

## 相关文档

- [BenGe 总览](../README.md)
- [前端说明](../frontend/README.md)
