# ATM Frontend

ATM 前端工程，覆盖欢迎页、登录页、主菜单页、查询余额页，以及第二次迭代的取款、存款、转账、修改密码页面。

## 运行

前端端口统一如下：

| 场景 | 命令 | 浏览器打开 |
| --- | --- | --- |
| 真实后端联调 | `npm run dev` 或 `npm run dev:real` | `http://127.0.0.1:5173/` |
| 前端 Mock 演示 | `npm run dev:mock` | `http://127.0.0.1:5174/` |

真实后端联调：

```bash
npm install
npm run dev
```

Mock 演示：

```bash
npm install
npm run dev:mock
```

## 环境变量

复制 `.env.example` 后按需修改：

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK=false
```

如果后端接口尚未就绪，优先运行 `npm run dev:mock`。该命令会读取 `.env.mock`，启用 `VITE_USE_MOCK=true`，并使用 `http://127.0.0.1:5174/`。

Mock 演示账号：

- 卡号：`6222020000000001`
- 密码：`123456`
