# BenGe

本目录整理自 `git@github.com:chantouRichard/BenGe.git`，按源仓库分支拆分为两个子项目：

- `frontend/`: 对应源仓库 `frontend` 分支，Vue 前端项目。详细说明见 `frontend/README.md`。
- `backend/`: 对应源仓库 `backend` 分支，Spring Boot 后端项目。

## 运行入口

### Frontend

```bash
cd frontend
npm install
npm run serve
```

### Backend

```bash
cd backend
mvn spring-boot:run
```

后端默认端口为 `7122`，运行前需要按 `backend/src/main/resources/application.yaml` 配置 MySQL、Redis 和相关服务参数。
