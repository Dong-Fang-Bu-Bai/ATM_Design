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

当前已接入的认证、账户与交易接口：

| 功能 | 方法 | 主路径 |
| --- | --- | --- |
| 登录 | `POST` | `/api/atm/auth/login` |
| 退出登录 | `POST` | `/api/atm/auth/logout` |
| 修改密码 | `POST` | `/api/atm/auth/change-password` |
| 基础账户信息 | `GET` | `/api/atm/accounts/profile?sessionId=...` |
| 完整账户信息 | `GET` | `/api/atm/accounts/full-info?sessionId=...` |
| 查询余额 | `GET` | `/api/atm/accounts/balance?sessionId=...` |
| 交易前校验 | `GET` | `/api/atm/accounts/validate-transaction?sessionId=...` |
| 取款 | `POST` | `/api/atm/transactions/withdraw` |
| 存款 | `POST` | `/api/atm/transactions/deposit` |
| 转账 | `POST` | `/api/atm/transactions/transfer` |
| 交易详情 | `GET` | `/api/atm/transactions/{transactionId}` |
| 交易流水 | `GET` | `/api/atm/transactions/history?sessionId=...&page=1&size=5` |
| 交易凭条 | `GET` | `/api/atm/receipts/{transactionId}?sessionId=...` |
| 设备状态 | `GET` | `/api/atm/device/status` |
| 吐钞能力检查 | `POST` | `/api/atm/device/cash-check` |

交易请求统一字段：

| 功能 | 请求字段 |
| --- | --- |
| 取款 | `sessionId`, `amount`, `printReceipt` |
| 存款 | `sessionId`, `amount`, `printReceipt` |
| 转账 | `sessionId`, `targetAccountNo`, `amount`, `printReceipt` |
| 交易详情 | path 参数 `transactionId` |
| 交易流水 | query 参数 `sessionId`, `page`, `size` |
| 交易凭条 | path 参数 `transactionId`，query 参数 `sessionId` |
| 吐钞能力检查 | `amount` |

详细字段以 `openapi-atm.yaml` 和 `atm-server-auth/src/api-test.http` 为准。

## 功能使用操作说明

推荐演示顺序：

1. 启动后端和前端，打开 `http://127.0.0.1:5173/`。
2. 使用卡号 `6222020000000001`、密码 `123456` 登录。
3. 在主菜单进入“设备状态”，确认设备 `ATM001` 为 `RUNNING`，并执行一次吐钞能力检查。
4. 回到主菜单执行取款、存款或转账。取款前系统会检查设备现金是否充足，取款成功后账户余额和设备可用现金会同步减少。
5. 交易成功后点击“查看凭条”，或进入“交易凭条”页面手动输入交易编号查询。
6. 进入“交易流水”页面查看当前账户的分页交易记录，并从流水列表进入单笔凭条。
7. 如需验证修改密码，先完成交易、流水和凭条演示，再执行“修改密码”。修改密码成功后当前 `sessionId` 会失效，需要使用新密码重新登录。
8. 演示结束后点击退卡退出。

后端单独验证可以使用 `atm-server-auth/src/api-test.http`。注意将登录响应中的 `sessionId` 替换到后续请求中，并将交易接口返回的 `transactionId` 替换到交易详情和凭条查询请求中。
