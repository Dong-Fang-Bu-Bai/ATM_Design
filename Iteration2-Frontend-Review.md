# Iteration 2 Frontend Review

## 1. 文档目的

本文档用于总结 ATM 系统第二次迭代中前端模块的实现范围、接口规范对齐情况、调试验证结论与后续联调注意事项，作为组内同步、阶段汇报和后续第三次迭代衔接材料。

---

## 2. 迭代依据

本轮前端工作主要依据以下项目材料：

- `总体迭代计划.png`：第二次细化迭代关注核心业务闭环，并服务于后续设计模型完善。
- `ATM_四人分工与三次迭代任务.md`：第二次迭代中前端模块要求实现取款、存款、转账、修改密码页面，对接认证、账户和交易接口，并优化交互提示。
- `ATM_UML作业方案.md`：代码交付建议中将取款、存款、转账、修改密码列为迭代二核心范围。
- `openapi-atm.yaml`：作为前端接口路径、请求字段与响应字段的主要规范来源。
- `Iteration1-Frontend-Review.md`：沿用第一次迭代的工程结构、mock 模式、路由守卫和 review 文档组织方式。

---

## 3. 迭代目标回顾

第二次迭代前端目标为：

- 实现取款页面
- 实现存款页面
- 实现转账页面
- 实现修改密码页面
- 对接认证、账户和交易接口
- 优化页面交互提示和错误提示
- 保持与 YAML 接口规范一致，便于后续后端完成后直接联调

本轮只做前端，不实现后端交易业务。

---

## 4. 本次完成内容

### 4.1 页面与路由

本轮新增了四个第二次迭代业务页面：

- `WithdrawView.vue`：取款页面
- `DepositView.vue`：存款页面
- `TransferView.vue`：转账页面
- `ChangePasswordView.vue`：修改密码页面

同时更新了 `router/index.js`，新增以下受登录保护的路由：

- `/withdraw`
- `/deposit`
- `/transfer`
- `/change-password`

主菜单中的取款、存款、转账、修改密码入口已从第一次迭代占位页切换到真实业务页面。交易凭条仍保留为第三次迭代占位入口。

### 4.2 接口封装

在 `src/api/atm.js` 中新增以下接口方法：

- `withdraw(payload)`
- `deposit(payload)`
- `transfer(payload)`
- `changePassword(payload)`

这些方法均按 `openapi-atm.yaml` 中定义的路径调用：

| 功能 | 方法 | 路径 |
| --- | --- | --- |
| 取款 | POST | `/api/atm/transactions/withdraw` |
| 存款 | POST | `/api/atm/transactions/deposit` |
| 转账 | POST | `/api/atm/transactions/transfer` |
| 修改密码 | POST | `/api/atm/auth/change-password` |

### 4.3 Mock 演示能力

由于当前后端交易模块仍处于骨架阶段，前端 mock 已扩展为可支持第二次迭代演示：

- 取款成功后扣减余额并返回 `remainingBalance`
- 存款成功后增加余额并返回 `updatedBalance`
- 转账支持目标账户校验、余额不足提示和余额扣减
- 修改密码支持原密码校验和新密码更新
- 交易结果返回 `transactionId`，便于后续第三次迭代凭条页面衔接

Mock 演示账号仍为：

- 卡号：`6222020000000001`
- 初始密码：`123456`
- 可用转账目标账户：`ACC20001`、`ACC30001`

### 4.4 交互与状态

本轮补充了以下交互能力：

- 金额必须大于 0
- 取款金额必须为 100 的整数倍
- 转账目标账户不能为空
- 修改密码必须为 6 位数字，并要求二次确认
- 交易成功后同步 Pinia 中的余额
- 账户信息预取时同步 `balance`，避免刚登录后交易页面显示余额为 0
- 本地存储解析增加容错，损坏的会话缓存会被清除，不再阻塞页面启动

### 4.5 YAML 对齐情况

本轮前端请求字段严格按 `openapi-atm.yaml` 现有规范实现：

| 功能 | 请求字段 |
| --- | --- |
| 取款 | `sessionId`, `amount`, `printReceipt` |
| 存款 | `sessionId`, `amount`, `printReceipt` |
| 转账 | `sessionId`, `targetAccountNo`, `amount`, `printReceipt` |
| 修改密码 | `sessionId`, `oldPassword`, `newPassword` |

审查结论：

- `openapi-atm.yaml` 中第二次迭代前端所需路径和字段已经完整。
- 本轮没有必要修改 YAML 结构。
- 已修正前端余额页文案中的旧路径，将 `/api/atm/account/balance` 统一为 `/api/atm/accounts/balance`。

---

## 5. 调试与验证过程

### 5.1 语法检查

对新增和变更的关键 JavaScript 文件执行了语法检查：

```bash
node --check src/api/atm.js
node --check src/api/mock.js
node --check src/router/index.js
```

结果：全部通过。

### 5.2 构建验证

执行前端生产构建：

```bash
npm run build
```

第一次在沙箱内执行时，`esbuild` 子进程启动被系统限制拦截，错误为 `spawn EPERM`。按审批流程在沙箱外重新执行后，构建成功：

```text
✓ built in 4.61s
```

构建输出仍存在 Vite 的 chunk size warning：

```text
Some chunks are larger than 500 kB after minification.
```

该问题主要来自 Element Plus 全量引入，第一次迭代也已有类似风险提示，不影响本轮功能构建通过。后续如需优化，可考虑按需引入组件或配置 manualChunks。

### 5.3 本地访问验证

按 mock 模式启动前端开发服务：

```bash
npm run dev:mock
```

沙箱内启动同样受 `spawn EPERM` 限制影响，按审批流程在沙箱外启动后，服务已监听：

```text
http://127.0.0.1:5174/
```

使用 `Invoke-WebRequest` 访问首页，返回状态码为 `200`。

---

## 6. 当前运行方式

在 `frontend/` 目录下执行：

| 场景 | 命令 | 浏览器打开 |
| --- | --- | --- |
| 真实后端联调 | `npm run dev` 或 `npm run dev:real` | `http://127.0.0.1:5173/` |
| 前端 Mock 演示 | `npm run dev:mock` | `http://127.0.0.1:5174/` |

```bash
npm install
npm run dev:mock
```

使用 mock 模式演示第二次迭代前端闭环。本次验证通过的本地访问地址为：

```text
http://127.0.0.1:5174/
```

若后端交易接口完成，可切换为真实接口：

```bash
VITE_USE_MOCK=false
VITE_API_BASE_URL=http://localhost:8080
```

---

## 7. 当前代码评审结论

本次前端第二次迭代已经满足任务文档中对同学 A 的阶段要求：

- 核心业务页面已完成
- 页面入口已接入主菜单
- 请求路径和字段已与 YAML 对齐
- mock 模式支持取款、存款、转账、修改密码演示
- 交易成功后能够同步余额并展示交易结果
- 前端生产构建通过

整体判断：本轮前端结果适合作为第二次迭代交付版本。

### 风险与后续注意事项

- 当前真实后端交易接口仍可能返回 `501 Not Implemented`，真实联调需等待后端第二次迭代交易业务完成。
- `change-password` 接口在当前后端代码中尚未实现，前端已按 YAML 预留并实现调用。
- 构建存在 chunk size warning，不阻塞交付，但建议第三次迭代或最终提交前优化 Element Plus 引入方式。
- 凭条、流水、设备状态仍属于第三次迭代范围，本轮仅保留入口或衔接字段。

---

## 8. 后续建议

第三次迭代前端建议优先推进：

- 交易流水页面
- 凭条展示页面
- 设备状态提示
- 真实后端联调记录
- 前端演示流程说明
- 打包体积优化
