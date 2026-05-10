const MOCK_ACCOUNT = {
  cardNo: '6222020000000001',
  password: '123456',
  sessionId: 'atm_session_demo_001',
  accountId: 10001,
  customerName: '张三',
  accountNo: 'ACC10001',
  accountType: '储蓄卡',
  idCard: '110101********1234',
  phone: '138****0000',
  status: '正常',
  createTime: '2026-03-01T10:20:30',
  balance: 5000
}

const MOCK_TARGET_ACCOUNTS = [
  {
    accountNo: 'ACC20001',
    customerName: '李四'
  },
  {
    accountNo: 'ACC30001',
    customerName: '王五'
  }
]

const MOCK_DEVICE = {
  atmCode: 'ATM001',
  location: '一号教学楼大厅',
  status: 'RUNNING',
  cashAvailable: 30000
}

const MOCK_HISTORY = [
  {
    transactionId: 'TX202603010003',
    transactionType: 'TRANSFER',
    amount: 300,
    transactionStatus: 'SUCCESS',
    transactionTime: '2026-03-01T11:20:30',
    balanceAfter: 5000,
    accountNo: MOCK_ACCOUNT.accountNo,
    targetAccountNo: 'ACC20001'
  },
  {
    transactionId: 'TX202603010002',
    transactionType: 'DEPOSIT',
    amount: 800,
    transactionStatus: 'SUCCESS',
    transactionTime: '2026-03-01T10:48:12',
    balanceAfter: 5300,
    accountNo: MOCK_ACCOUNT.accountNo
  },
  {
    transactionId: 'TX202603010001',
    transactionType: 'WITHDRAW',
    amount: 500,
    transactionStatus: 'SUCCESS',
    transactionTime: '2026-03-01T09:36:45',
    balanceAfter: 4500,
    accountNo: MOCK_ACCOUNT.accountNo
  }
]

let transactionSequence = MOCK_HISTORY.length + 1

function createTransactionId() {
  const now = new Date()
  const date = [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0')
  ].join('')
  const sequence = String(transactionSequence).padStart(4, '0')
  transactionSequence += 1

  return `TX${date}${sequence}`
}

function createTransactionRecord(type, amount, balanceAfter, extra = {}) {
  const record = {
    transactionId: createTransactionId(),
    transactionType: type,
    amount,
    transactionStatus: 'SUCCESS',
    transactionTime: new Date().toISOString(),
    balanceAfter,
    accountNo: MOCK_ACCOUNT.accountNo,
    ...extra
  }

  MOCK_HISTORY.unshift(record)
  return record
}

function assertSession(sessionId) {
  if (sessionId !== MOCK_ACCOUNT.sessionId) {
    throw createApiError('会话已失效，请重新登录', 401)
  }
}

function parseAmount(amount) {
  const value = Number(amount)

  if (!Number.isFinite(value) || value <= 0) {
    throw createApiError('交易金额必须大于 0', 400)
  }

  return Number(value.toFixed(2))
}

function sleep(timeout = 450) {
  return new Promise((resolve) => window.setTimeout(resolve, timeout))
}

function createApiError(message, status = 400) {
  const error = new Error(message)
  error.response = {
    status,
    data: {
      code: status,
      message,
      data: null,
      timestamp: Date.now()
    }
  }

  return error
}

function createResponse(data, message = 'success', code = 200) {
  return {
    code,
    message,
    data,
    timestamp: Date.now()
  }
}

export async function mockLogin(payload) {
  await sleep()

  if (
    payload.cardNo !== MOCK_ACCOUNT.cardNo ||
    payload.password !== MOCK_ACCOUNT.password
  ) {
    throw createApiError('卡号或密码错误，请重试', 401)
  }

  MOCK_ACCOUNT.sessionId = `atm_session_demo_${Date.now()}`

  return createResponse(
    {
      sessionId: MOCK_ACCOUNT.sessionId,
      accountId: MOCK_ACCOUNT.accountId
    },
    '登录成功'
  )
}

export async function mockLogout(sessionId) {
  await sleep(240)
  assertSession(sessionId)
  MOCK_ACCOUNT.sessionId = ''

  return createResponse(null, '退卡成功')
}

export async function mockGetBalance(sessionId) {
  await sleep(320)

  assertSession(sessionId)

  return createResponse(
    {
      balance: MOCK_ACCOUNT.balance
    },
    '查询成功'
  )
}

export async function mockGetProfile(sessionId) {
  await sleep(320)

  assertSession(sessionId)

  return createResponse(
    {
      customerName: MOCK_ACCOUNT.customerName,
      cardNo: MOCK_ACCOUNT.cardNo,
      idCard: MOCK_ACCOUNT.idCard,
      phone: MOCK_ACCOUNT.phone,
      accountNo: MOCK_ACCOUNT.accountNo,
      accountType: MOCK_ACCOUNT.accountType,
      balance: MOCK_ACCOUNT.balance,
      createTime: MOCK_ACCOUNT.createTime,
      status: MOCK_ACCOUNT.status
    },
    '查询成功'
  )
}

export async function mockValidateTransactionSession(sessionId) {
  await sleep(260)
  assertSession(sessionId)

  return createResponse(
    {
      valid: true,
      cardNo: MOCK_ACCOUNT.cardNo,
      accountId: MOCK_ACCOUNT.accountId,
      accountNo: MOCK_ACCOUNT.accountNo,
      balance: MOCK_ACCOUNT.balance,
      customerName: MOCK_ACCOUNT.customerName,
      message: '验证通过',
      accountType: 1,
      status: MOCK_ACCOUNT.status
    },
    '验证通过'
  )
}

export async function mockWithdraw(payload) {
  await sleep(420)
  assertSession(payload.sessionId)

  const amount = parseAmount(payload.amount)

  if (amount % 100 !== 0) {
    throw createApiError('取款金额应为 100 的整数倍', 400)
  }

  if (amount > MOCK_ACCOUNT.balance) {
    throw createApiError('账户余额不足，无法完成取款', 403)
  }

  if (amount > MOCK_DEVICE.cashAvailable) {
    throw createApiError('ATM 设备现金不足，无法完成取款', 409)
  }

  MOCK_ACCOUNT.balance = Number((MOCK_ACCOUNT.balance - amount).toFixed(2))
  MOCK_DEVICE.cashAvailable = Number((MOCK_DEVICE.cashAvailable - amount).toFixed(2))
  const record = createTransactionRecord('WITHDRAW', amount, MOCK_ACCOUNT.balance)

  return createResponse({
    transactionId: record.transactionId,
    success: true,
    message: payload.printReceipt ? '取款成功，已生成凭条请求' : '取款成功',
    remainingBalance: MOCK_ACCOUNT.balance
  })
}

export async function mockDeposit(payload) {
  await sleep(420)
  assertSession(payload.sessionId)

  const amount = parseAmount(payload.amount)
  MOCK_ACCOUNT.balance = Number((MOCK_ACCOUNT.balance + amount).toFixed(2))
  const record = createTransactionRecord('DEPOSIT', amount, MOCK_ACCOUNT.balance)

  return createResponse({
    transactionId: record.transactionId,
    success: true,
    message: payload.printReceipt ? '存款成功，已生成凭条请求' : '存款成功',
    updatedBalance: MOCK_ACCOUNT.balance
  })
}

export async function mockTransfer(payload) {
  await sleep(420)
  assertSession(payload.sessionId)

  const amount = parseAmount(payload.amount)
  const target = MOCK_TARGET_ACCOUNTS.find(
    (account) => account.accountNo === payload.targetAccountNo
  )

  if (payload.targetAccountNo === MOCK_ACCOUNT.accountNo) {
    throw createApiError('不能向当前账户转账', 400)
  }

  if (!target) {
    throw createApiError('目标账户不存在，请核对后重试', 404)
  }

  if (amount > MOCK_ACCOUNT.balance) {
    throw createApiError('账户余额不足，无法完成转账', 403)
  }

  MOCK_ACCOUNT.balance = Number((MOCK_ACCOUNT.balance - amount).toFixed(2))
  const record = createTransactionRecord('TRANSFER', amount, MOCK_ACCOUNT.balance, {
    targetAccountNo: target.accountNo
  })

  return createResponse({
    transactionId: record.transactionId,
    success: true,
    message: `转账成功，收款账户：${target.accountNo}`,
    remainingBalance: MOCK_ACCOUNT.balance
  })
}

export async function mockChangePassword(payload) {
  await sleep(360)
  assertSession(payload.sessionId)

  if (payload.oldPassword !== MOCK_ACCOUNT.password) {
    throw createApiError('原密码错误，请重新输入', 401)
  }

  if (payload.newPassword === MOCK_ACCOUNT.password) {
    throw createApiError('新密码不能与原密码一致', 400)
  }

  MOCK_ACCOUNT.password = payload.newPassword
  MOCK_ACCOUNT.sessionId = ''

  return createResponse(
    {
      success: true,
      message: '密码修改成功，请重新登录'
    },
    '密码修改成功，请重新登录'
  )
}

export async function mockGetTransactionHistory(sessionId, params = {}) {
  await sleep(320)
  assertSession(sessionId)

  const page = Math.max(Number(params.page) || 1, 1)
  const size = Math.min(Math.max(Number(params.size) || 10, 1), 50)
  const start = (page - 1) * size
  const records = MOCK_HISTORY.slice(start, start + size).map((record) => ({
    transactionId: record.transactionId,
    transactionType: record.transactionType,
    amount: record.amount,
    transactionStatus: record.transactionStatus,
    transactionTime: record.transactionTime
  }))

  return createResponse(
    {
      page,
      size,
      total: MOCK_HISTORY.length,
      records
    },
    '查询成功'
  )
}

export async function mockGetReceipt(transactionId, sessionId) {
  await sleep(300)
  assertSession(sessionId)

  const record = MOCK_HISTORY.find((item) => item.transactionId === transactionId)

  if (!record) {
    throw createApiError('凭条不存在，请核对交易编号', 404)
  }

  return createResponse(
    {
      transactionId: record.transactionId,
      type: record.transactionType,
      amount: record.amount,
      balanceAfter: record.balanceAfter,
      time: record.transactionTime,
      accountNo: record.accountNo
    },
    '获取成功'
  )
}

export async function mockGetDeviceStatus() {
  await sleep(260)

  return createResponse(
    {
      atmCode: MOCK_DEVICE.atmCode,
      location: MOCK_DEVICE.location,
      status: MOCK_DEVICE.status,
      cashAvailable: MOCK_DEVICE.cashAvailable
    },
    '查询成功'
  )
}

export async function mockCheckCashAvailability(payload) {
  await sleep(260)

  const amount = parseAmount(payload.amount)
  const available = MOCK_DEVICE.status === 'RUNNING' && amount <= MOCK_DEVICE.cashAvailable

  return createResponse(
    {
      available,
      amount,
      cashAvailable: MOCK_DEVICE.cashAvailable
    },
    available ? '检查成功' : '设备现金不足'
  )
}

