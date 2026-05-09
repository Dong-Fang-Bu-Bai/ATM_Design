# ATM 认证、账户与交易服务功能说明

## 统一口径

- 主会话字段：`sessionId`
- 兼容字段：`token`，仅用于兼容旧请求，不作为新文档和前端主字段
- 主账户路径：`/api/atm/accounts/*`
- 兼容账户路径：`/api/atm/account/*`
- 交易流水对外字段：`transactionId`
- 转账目标账户字段：`targetAccountNo`
- 交易请求字段：取款、存款使用 `sessionId`、`amount`、`printReceipt`；转账额外使用 `targetAccountNo`
- 统一响应：`code`、`message`、`data`、`timestamp`
- 统一异常：业务异常通过 `ApiException` 交给 `GlobalExceptionHandler` 转换为结构化响应

## 已接入接口

| 功能 | 方法 | 主路径 | 说明 |
| --- | --- | --- | --- |
| 登录 | `POST` | `/api/atm/auth/login` | 成功后返回 `sessionId` 和 `accountId` |
| 退出登录 | `POST` | `/api/atm/auth/logout` | 请求体传 `sessionId`，兼容 `token` |
| 修改密码 | `POST` | `/api/atm/auth/change-password` | 请求体传 `sessionId`、`oldPassword`、`newPassword`；成功后当前会话失效 |
| 基础账户信息 | `GET` | `/api/atm/accounts/profile?sessionId=...` | 返回卡号、姓名、脱敏身份证、账号、余额、账户类型、开户时间 |
| 完整账户信息 | `GET` | `/api/atm/accounts/full-info?sessionId=...` | 在基础信息上补充脱敏手机号和账户状态 |
| 查询余额 | `GET` | `/api/atm/accounts/balance?sessionId=...` | 返回结构化余额对象 `{ "balance": ... }` |
| 交易前校验 | `GET` | `/api/atm/accounts/validate-transaction?sessionId=...` | 返回账户是否可用于取款、存款、转账 |
| 取款 | `POST` | `/api/atm/transactions/withdraw` | 请求体传 `sessionId`、`amount`、`printReceipt`；成功后返回 `transactionId` 和剩余余额 |
| 存款 | `POST` | `/api/atm/transactions/deposit` | 请求体传 `sessionId`、`amount`、`printReceipt`；成功后返回 `transactionId` 和更新后余额 |
| 转账 | `POST` | `/api/atm/transactions/transfer` | 请求体传 `sessionId`、`targetAccountNo`、`amount`、`printReceipt`；成功后返回 `transactionId` 和剩余余额 |
| 交易详情 | `GET` | `/api/atm/transactions/{transactionId}` | 根据对外交易流水号查询交易详情 |

## 与交易模块协作

交易模块在执行取款、存款、转账前可调用：

```http
GET http://localhost:8080/api/atm/accounts/validate-transaction?sessionId=REPLACE_WITH_SESSION_ID
```

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": true,
    "cardNo": "6222020000000001",
    "accountId": 10001,
    "accountNo": "ACC10001",
    "balance": 5000.00,
    "customerName": "张三",
    "message": "验证通过",
    "accountType": 1,
    "status": "正常"
  },
  "timestamp": 1778055092752
}
```

## 主要代码位置

| 类型 | 路径 |
| --- | --- |
| 统一响应 | `src/main/java/com/atm/atmserver/common/Result.java` |
| 全局异常 | `src/main/java/com/atm/atmserver/common/GlobalExceptionHandler.java` |
| 会话校验 | `src/main/java/com/atm/atmserver/util/SessionValidator.java` |
| Token 管理 | `src/main/java/com/atm/atmserver/util/TokenManager.java` |
| 认证接口 | `src/main/java/com/atm/atmserver/controller/AuthController.java` |
| 账户接口 | `src/main/java/com/atm/atmserver/controller/AccountController.java` |
| HTTP 示例 | `src/api-test.http` |
