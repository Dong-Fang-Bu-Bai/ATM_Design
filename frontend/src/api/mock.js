const MOCK_ACCOUNT = {
  cardNo: '6222020000000001',
  password: '123456',
  sessionId: 'atm_session_demo_001',
  accountId: 10001,
  customerName: '张三',
  accountNo: 'ACC10001',
  accountType: '储蓄卡',
  idCard: '110101********1234',
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

let transactionSequence = 1

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
      message
    }
  }

  return error
}

export async function mockLogin(payload) {
  await sleep()

  if (
    payload.cardNo !== MOCK_ACCOUNT.cardNo ||
    payload.password !== MOCK_ACCOUNT.password
  ) {
    throw createApiError('卡号或密码错误，请重试', 401)
  }

  return {
    code: 200,
    message: '登录成功',
    data: {
      sessionId: MOCK_ACCOUNT.sessionId,
      accountId: MOCK_ACCOUNT.accountId
    }
  }
}

export async function mockLogout() {
  await sleep(240)

  return {
    code: 200,
    message: '退卡成功',
    data: null
  }
}

export async function mockGetBalance(sessionId) {
  await sleep(320)

  assertSession(sessionId)

  return {
    code: 200,
    message: '查询成功',
    data: {
      balance: MOCK_ACCOUNT.balance
    }
  }
}

export async function mockGetProfile(sessionId) {
  await sleep(320)

  assertSession(sessionId)

  return {
    code: 200,
    message: '查询成功',
    data: {
      customerName: MOCK_ACCOUNT.customerName,
      cardNo: MOCK_ACCOUNT.cardNo,
      idCard: MOCK_ACCOUNT.idCard,
      accountNo: MOCK_ACCOUNT.accountNo,
      accountType: MOCK_ACCOUNT.accountType,
      balance: MOCK_ACCOUNT.balance
    }
  }
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

  MOCK_ACCOUNT.balance = Number((MOCK_ACCOUNT.balance - amount).toFixed(2))

  return {
    code: 200,
    message: 'success',
    data: {
      transactionId: createTransactionId(),
      success: true,
      message: payload.printReceipt ? '取款成功，已生成凭条请求' : '取款成功',
      remainingBalance: MOCK_ACCOUNT.balance
    }
  }
}

export async function mockDeposit(payload) {
  await sleep(420)
  assertSession(payload.sessionId)

  const amount = parseAmount(payload.amount)
  MOCK_ACCOUNT.balance = Number((MOCK_ACCOUNT.balance + amount).toFixed(2))

  return {
    code: 200,
    message: 'success',
    data: {
      transactionId: createTransactionId(),
      success: true,
      message: payload.printReceipt ? '存款成功，已生成凭条请求' : '存款成功',
      updatedBalance: MOCK_ACCOUNT.balance
    }
  }
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

  return {
    code: 200,
    message: 'success',
    data: {
      transactionId: createTransactionId(),
      success: true,
      message: `转账成功，收款账户：${target.accountNo}`,
      remainingBalance: MOCK_ACCOUNT.balance
    }
  }
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

  return {
    code: 200,
    message: '修改成功',
    data: null
  }
}

