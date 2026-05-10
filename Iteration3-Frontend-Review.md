# Iteration 3 Frontend Review

## 1. 文档目的

本文档用于总结 ATM 系统第三次迭代中前端模块的计划、实现范围、接口规范对齐情况、构建验证结论与后续后端联调注意事项，作为组内同步、阶段汇报和最终答辩材料的前端部分依据。

---

## 2. 迭代依据

本轮前端工作主要依据以下项目材料：

- `总体迭代计划.png`：第四章“细化迭代 3”要求完成状态机建模、模型完善和完整文档汇总，前端侧需要支撑最终演示流程。
- `ATM_四人分工与三次迭代任务.md`：第三次迭代中前端模块要求实现交易流水页面、凭条展示页面、完善设备状态提示、统一整体页面风格，并完成页面测试与演示流程整理。
- `软件分析设计与建模实验要求.doc`：实验要求按 UP 过程分阶段完善制品，最终形成完整的系统分析与设计问题；ATM 功能范围包含存款、取款、查询余额、转账、修改密码、打印凭条和退卡。
- `ATM_UML作业方案.md`：代码交付建议中将打印凭条、交易流水、异常处理、系统部署与演示列为迭代三范围。
- `openapi-atm.yaml`：作为前端接口路径、请求字段与响应字段的主要规范来源。
- `Iteration2-Frontend-Review.md`：沿用第二次迭代的工程结构、mock 模式、路由守卫、接口封装和 review 文档组织方式。

---

## 3. 前端第三次迭代计划

### 3.1 迭代目标

第三次迭代前端目标为：

- 实现交易流水页面，支持按当前会话账户分页查看交易记录。
- 实现凭条展示页面，支持交易成功后直接跳转凭条，也支持手动输入交易编号查询。
- 完善设备状态提示，支持查看 ATM 终端运行状态和可用现金。
- 在取款前增加吐钞能力检查，提前提示设备现金不足问题。
- 将主菜单从第二次迭代核心交易入口扩展为最终演示入口。
- 保持 `sessionId`、`transactionId`、`targetAccountNo` 和 `/api/atm/accounts/*` 的公开契约口径。

### 3.2 页面与路由计划

| 页面 | 路由 | 迭代职责 |
| --- | --- | --- |
| 交易流水页 | `/history` | 查询当前账户交易流水，支持进入凭条 |
| 交易凭条页 | `/receipt/:transactionId?` | 查询并展示单笔交易凭条 |
| 设备状态页 | `/device-status` | 展示 ATM 状态与吐钞能力检查 |
| 主菜单页 | `/menu` | 汇总余额、核心交易、流水、凭条、设备入口 |

### 3.3 接口计划

| 功能 | 方法 | 路径 | 前端请求口径 |
| --- | --- | --- | --- |
| 交易流水 | GET | `/api/atm/transactions/history` | query：`sessionId`, `page`, `size` |
| 交易凭条 | GET | `/api/atm/receipts/{transactionId}` | path：`transactionId`；query：`sessionId` |
| 设备状态 | GET | `/api/atm/device/status` | 无请求体 |
| 吐钞能力检查 | POST | `/api/atm/device/cash-check` | body：`amount` |

凭条接口原 YAML 只包含 `transactionId` 路径参数。本轮为了后续后端实现时能按当前登录会话约束凭条访问，已补充 `sessionId` 查询参数，使其与交易流水接口保持同一会话口径。

---

## 4. 本次完成内容

### 4.1 页面与路由

本轮新增三个第三次迭代页面：

- `TransactionHistoryView.vue`：交易流水页面
- `ReceiptView.vue`：交易凭条页面
- `DeviceStatusView.vue`：设备状态页面

同时更新了 `router/index.js`，新增以下受登录保护的路由：

- `/history`
- `/receipt/:transactionId?`
- `/device-status`

主菜单中的“交易流水”“交易凭条”“设备状态”入口已切换为真实页面，不再停留在占位页。

### 4.2 接口封装

在 `src/api/atm.js` 中新增以下接口方法：

- `getTransactionHistory(sessionId, params)`
- `getReceipt(transactionId, sessionId)`
- `getDeviceStatus()`
- `checkCashAvailability(payload)`

这些方法均按 `openapi-atm.yaml` 的第三次迭代接口路径调用，并继续支持 mock 模式和真实后端联调模式切换。

### 4.3 Mock 演示能力

`src/api/mock.js` 已补充第三次迭代所需数据与行为：

- 交易成功后写入 mock 交易流水。
- 流水页面支持分页返回。
- 凭条页面可根据 `transactionId` 查询交易凭条。
- 设备状态返回 ATM 编号、位置、运行状态和可用现金。
- 取款前可检查 ATM 现金是否足够。
- 取款成功后同步扣减 mock 账户余额与 ATM 可用现金。

Mock 模式下可继续使用账号：

- 卡号：`6222020000000001`
- 密码：`123456`
- 可用转账目标账户：`ACC20001`、`ACC30001`

### 4.4 交互与状态

本轮补充了以下交互能力：

- 交易流水列表展示交易时间、交易编号、类型、金额、状态和凭条入口。
- 凭条页面支持从流水或交易结果直接进入，也支持手动输入编号查询。
- 取款、存款、转账成功后提供“查看凭条”操作。
- 余额页增加“查看流水”入口。
- 主菜单展示设备状态和可用现金提示。
- 设备状态页支持手动刷新和指定金额的吐钞能力检查。
- 取款提交前先调用设备吐钞能力检查，设备现金不足时不继续发起取款交易。

### 4.5 YAML 对齐情况

本轮前端新增请求字段和路径与 `openapi-atm.yaml` 对齐：

| 功能 | 请求字段 |
| --- | --- |
| 交易流水 | `sessionId`, `page`, `size` |
| 交易凭条 | `transactionId`, `sessionId` |
| 设备状态 | 无请求体 |
| 吐钞能力检查 | `amount` |

审查结论：

- YAML 中已存在第三次迭代所需的流水、凭条、设备状态和吐钞检查接口。
- 本轮仅补充凭条查询的 `sessionId` 参数，使后续后端实现能够按当前会话做访问控制。
- 前端仍沿用第二次迭代统一后的公开契约：`sessionId`、`transactionId`、`targetAccountNo`、`/api/atm/accounts/*`。

---

## 5. 调试与验证过程

### 5.1 语法检查

对关键 JavaScript 文件执行了语法检查：

```bash
node --check frontend/src/api/atm.js
node --check frontend/src/api/mock.js
node --check frontend/src/router/index.js
node --check frontend/src/utils/format.js
```

结果：全部通过。

### 5.2 构建验证

执行前端生产构建：

```bash
npm run build
```

构建结果：

```text
✓ built in 4.60s
```

构建输出中仍存在 Vite 的 chunk size warning：

```text
Some chunks are larger than 500 kB after minification.
```

该 warning 延续自前两次迭代，主要来自 Element Plus 全量引入，不影响本轮功能构建通过。若后续需要进一步优化，可在最终提交前考虑按需引入组件或配置 `manualChunks`。

---

## 6. 当前运行方式

在 `frontend/` 目录下执行：

| 场景 | 命令 | 浏览器打开 |
| --- | --- | --- |
| 真实后端联调 | `npm run dev` 或 `npm run dev:real` | `http://127.0.0.1:5173/` |
| 前端 Mock 演示 | `npm run dev:mock` | `http://127.0.0.1:5174/` |

建议第三次迭代 mock 演示流程：

1. 使用卡号 `6222020000000001`、密码 `123456` 登录。
2. 在主菜单查看设备状态提示。
3. 进入设备状态页，执行一次吐钞能力检查。
4. 执行取款、存款或转账交易。
5. 在交易结果中点击“查看凭条”。
6. 返回交易流水页，查看交易记录并再次进入凭条。
7. 返回主菜单后退卡退出。

---

## 7. 当前代码评审结论

本次前端第三次迭代已经满足任务文档中对同学 A 的阶段要求：

- 交易流水页面已完成。
- 凭条展示页面已完成。
- 设备状态提示已完成。
- 主菜单和关键交易页面已串联第三次迭代入口。
- 请求路径和字段已与 YAML 对齐。
- mock 模式支持第三次迭代完整演示。
- 前端生产构建通过。

整体判断：本轮前端结果适合作为第三次迭代交付版本，并已为后端后续完成流水、凭条和设备接口保留一致的联调契约。

### 风险与后续注意事项

- 第三次迭代后端接口仍需按 `openapi-atm.yaml` 完成真实实现，尤其是 `/api/atm/transactions/history`、`/api/atm/receipts/{transactionId}`、`/api/atm/device/status` 和 `/api/atm/device/cash-check`。
- 凭条查询已要求携带 `sessionId`，后端实现时应校验该交易是否属于当前会话账户。
- 当前构建仍有大包体积 warning，不阻塞课程交付，但建议最终版本优化 Element Plus 引入方式。
- 当前审查范围限定为前端第三次迭代；后端真实业务闭环和 UML 终稿仍需分别审查。
