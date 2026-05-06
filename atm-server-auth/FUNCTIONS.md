# ATM 认证服务功能说明

## 已完成功能

### 1. 完善账户信息查询接口

#### 1.1 基础账户信息查询
- **接口**: `GET /api/atm/account/info?token=xxx`
- **功能**: 查询账户基本信息（卡号、姓名、脱敏身份证、账号、余额、账户类型、开户时间）
- **鉴权**: 需要有效的Token

#### 1.2 完整账户信息查询（新增）
- **接口**: `GET /api/atm/account/full-info?token=xxx`
- **功能**: 查询账户完整信息（包含手机号脱敏、账户状态等更多详细信息）
- **鉴权**: 需要有效的Token
- **新增字段**: 
  - phone: 脱敏手机号
  - status: 账户状态

### 2. 实现修改密码接口

- **接口**: `POST /api/atm/auth/change-password?token=xxx`
- **请求体**:
```json
{
  "oldPassword": "原密码",
  "newPassword": "新密码（至少6位）"
}
```
- **功能**:
  - 验证原密码正确性
  - 验证新密码长度（至少6位）
  - 修改成功后使当前Token失效，需要重新登录
- **鉴权**: 需要有效的Token

### 3. 补充会话校验逻辑

#### 3.1 新增 SessionValidator 工具类
- **位置**: `com.atm.atmserver.util.SessionValidator`
- **功能**:
  - 统一Token验证逻辑
  - 验证Token有效性并获取卡号
  - 提供规范的错误返回

#### 3.2 增强 TokenManager
- **新增方法**: `getTokenByCardNo(String cardNo)`
- **功能**: 根据卡号获取Token（用于修改密码等场景）

#### 3.3 统一鉴权方式
- 所有需要鉴权的接口都使用 SessionValidator 进行Token验证
- 统一的异常处理和错误返回

### 4. 交易前身份和账户校验（配合同学C）

- **接口**: `GET /api/atm/account/validate-transaction?token=xxx`
- **响应数据结构**: `TransactionValidationResponse`
  - valid: 验证是否通过
  - cardNo: 银行卡号
  - accountId: 账户ID
  - accountNo: 账号
  - balance: 账户余额
  - customerName: 客户姓名
  - accountType: 账户类型
  - status: 账户状态
  - message: 验证结果消息

- **验证内容**:
  1. 卡号是否存在
  2. 账户是否存在
  3. 客户信息是否存在
  4. 账户状态是否正常
  
- **用途**: 同学C的交易模块可以调用此接口进行交易前的身份和账户验证

### 5. 优化异常返回格式

#### 5.1 增强 Result 类
- **新增字段**: timestamp（时间戳）
- **新增方法**:
  - `success(T data, String message)`: 成功响应（带自定义消息）
  - `error(Integer code, String msg)`: 错误响应（自定义状态码）
  - `badRequest(String msg)`: 参数错误（400）
  - `unauthorized(String msg)`: 未授权（401）
  - `forbidden(String msg)`: 禁止访问（403）
  - `notFound(String msg)`: 资源不存在（404）

#### 5.2 全局异常处理器
- **位置**: `com.atm.atmserver.config.GlobalExceptionHandler`
- **功能**:
  - 统一处理运行时异常
  - 统一处理系统异常
  - 返回规范的错误响应格式

#### 5.3 统一错误响应
- Token验证失败返回 401 状态码
- 参数错误返回 400 状态码
- 系统错误返回 500 状态码
- 所有响应包含时间戳

## 项目结构

```
src/main/java/com/atm/atmserver/
├── common/
│   └── Result.java                    # 统一响应结构（已优化）
├── config/
│   └── GlobalExceptionHandler.java    # 全局异常处理器（新增）
├── controller/
│   ├── AuthController.java            # 认证控制器（已更新）
│   └── AccountController.java         # 账户控制器（已更新）
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── LogoutRequest.java
│   ├── AccountInfoResponse.java
│   ├── FullAccountInfoResponse.java   # 完整账户信息响应（新增）
│   ├── ChangePasswordRequest.java     # 修改密码请求（新增）
│   ├── ChangePasswordResponse.java    # 修改密码响应（新增）
│   └── TransactionValidationResponse.java  # 交易验证响应（新增）
├── entity/
│   ├── Account.java
│   ├── BankCard.java
│   └── Customer.java
├── mapper/
│   ├── AccountMapper.java
│   ├── BankCardMapper.java
│   └── CustomerMapper.java
├── service/
│   ├── AuthService.java               # 认证服务接口（已更新）
│   ├── AccountService.java            # 账户服务接口（已更新）
│   └── impl/
│       ├── AuthServiceImpl.java       # 认证服务实现（已更新）
│       └── AccountServiceImpl.java    # 账户服务实现（已更新）
└── util/
    ├── TokenManager.java              # Token管理器（已增强）
    └── SessionValidator.java          # 会话校验器（新增）
```

## API 测试

使用项目中的 `api-test.http` 文件进行接口测试，包含所有接口的测试用例。

## 与同学C的协作

同学C的交易模块可以通过以下方式调用本服务的验证功能：

1. **交易前验证**: 调用 `/api/atm/account/validate-transaction?token=xxx` 接口
2. **获取验证结果**: 从 `TransactionValidationResponse` 中获取账户信息和验证状态
3. **判断是否允许交易**: 根据 `valid` 字段和 `balance` 等信息决定是否执行交易

示例：
```java
// 同学C的代码中可以这样调用
GET http://localhost:8080/api/atm/account/validate-transaction?token={token}

// 返回示例
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": true,
    "cardNo": "6222020000000001",
    "accountId": 1,
    "accountNo": "ACC001",
    "balance": 10000.00,
    "customerName": "张三",
    "accountType": 1,
    "status": "正常",
    "message": "验证通过"
  },
  "timestamp": 1234567890
}
```

## 注意事项

1. 所有需要鉴权的接口都必须传递有效的Token
2. 修改密码后Token会失效，需要重新登录
3. 密码长度不能少于6位
4. 敏感信息（身份证号、手机号）已进行脱敏处理
5. 所有响应都包含时间戳字段
6. 错误响应使用合适的HTTP状态码（401、400、500等）