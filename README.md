# ATM_Design
UML实验小组

## 总体迭代计划

<p align="center">
  <img src="./总体迭代计划.png" alt="总体迭代计划" width="700" />
</p>


# 请大家注意pull request的查收，在main更新后及时merge到自己的分支哦！
1.  创建仓库。 
2.	所有人用 GitHub Desktop 把仓库 clone 到自己电脑。
3.	每个人新建自己的分支。
4.	每个人只在自己的分支上改。
5.	改完先 commit，再 push。
6.	到 GitHub 网页上开 Pull Request。
7.	组长或队友审核后再 merge。
8.	其他人 pull，拿到最新版本。

## 本地完整运行

Windows 下可以直接双击或在 PowerShell 执行：

```powershell
.\start-dev.cmd
```

脚本会启动两个窗口：

- 后端：`http://localhost:8080/api/atm`，使用 `dev` 内存数据库并自动初始化演示账号
- 前端真实联调：`http://127.0.0.1:5173/`

前端地址统一口径：

| 场景 | 命令 | 浏览器打开 |
| --- | --- | --- |
| 真实后端联调 | `npm run dev` 或 `npm run dev:real` | `http://127.0.0.1:5173/` |
| 只体验前端 Mock 流程 | `npm run dev:mock` | `http://127.0.0.1:5174/` |

演示账号：

- 卡号：`6222020000000001`
- 密码：`123456`

如果手动启动，先开后端：

```powershell
cd .\atm-server-auth
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

再开一个终端启动前端：

```powershell
cd .\frontend
npm install
npm run dev:real
```

前端真实联调服务会通过 Vite proxy 把 `/api/atm/**` 转发到 `http://localhost:8080`，浏览器里只需要打开 `http://127.0.0.1:5173/`。

只体验前端 Mock 流程可以运行：

```powershell
cd .\frontend
npm install
npm run dev:mock
```

Mock 模式不依赖后端交易接口，浏览器打开 `http://127.0.0.1:5174/`。

## 当前接口口径

对外主契约统一使用 `sessionId`，旧字段 `token` 仅作为后端兼容别名保留。后端所有 `Result` 响应均包含 `code`、`message`、`data`、`timestamp`。

当前已接入的认证与账户接口：

| 功能 | 方法 | 主路径 |
| --- | --- | --- |
| 登录 | `POST` | `/api/atm/auth/login` |
| 退出登录 | `POST` | `/api/atm/auth/logout` |
| 修改密码 | `POST` | `/api/atm/auth/change-password` |
| 基础账户信息 | `GET` | `/api/atm/accounts/profile?sessionId=...` |
| 完整账户信息 | `GET` | `/api/atm/accounts/full-info?sessionId=...` |
| 查询余额 | `GET` | `/api/atm/accounts/balance?sessionId=...` |
| 交易前校验 | `GET` | `/api/atm/accounts/validate-transaction?sessionId=...` |

详细字段以 `openapi-atm.yaml` 和 `atm-server-auth/src/api-test.http` 为准。
