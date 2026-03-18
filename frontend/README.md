# ATM Frontend

第一次迭代前端工程，覆盖欢迎页、登录页、主菜单页、查询余额页，以及后续功能入口占位。

## 运行

```bash
npm install
npm run dev
```

## 环境变量

复制 `.env.example` 后按需修改：

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK=false
```

如果后端接口尚未就绪，可将 `VITE_USE_MOCK` 改为 `true`，使用以下演示账号：

- 卡号：`6222020000000001`
- 密码：`123456`
