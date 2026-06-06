# Contributing

感谢你关注本仓库。这里既包含软件构造课程作业，也包含 BenGe 全栈项目。欢迎提交文档补充、问题修复、运行指南、工程化改进和功能优化。

## 贡献范围

- 修复 README、运行说明、配置说明或目录说明中的问题。
- 补充课程作业说明、测试数据说明或项目复盘内容。
- 修复 BenGe 前端或后端的可复现缺陷。
- 改进前后端联调、部署、测试和安全配置。
- 补充 API、数据结构或协作消息格式文档。

## 提交前检查

提交 Pull Request 前请尽量完成以下检查：

```bash
git status --short
```

前端相关改动：

```bash
cd BenGe/frontend
npm install
npm run lint
npm run build
```

后端相关改动：

```bash
cd BenGe/backend
mvn test
```

如果因为缺少 MySQL、Redis 或外部 AI 服务导致测试无法运行，请在 PR 描述中说明原因和已完成的手动验证。

## 分支与提交

- 建议从 `master` 新建功能分支，例如 `docs/readme-refresh` 或 `fix/backend-login`。
- 提交信息尽量简短明确，例如 `docs: improve BenGe backend guide`。
- 一次 PR 聚焦一个主题，避免混合无关改动。

## 文档规范

- Markdown 标题层级保持清晰，不跳级。
- 命令示例使用 fenced code block。
- 涉及配置和密钥时只写环境变量名，不写真实值。
- 修改 API、WebSocket 消息或数据结构时，同步更新相关 README 或 `BenGe/frontend/docs/DataFormats.md`。

## 安全要求

不要提交以下内容：

- 数据库真实密码。
- JWT secret。
- 第三方 API key、API password、token。
- ngrok token。
- 生产环境地址、内部服务器地址或私有凭据。

如发现敏感信息已被提交，请参考 [SECURITY.md](SECURITY.md) 处理。
