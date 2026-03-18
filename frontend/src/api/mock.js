const MOCK_ACCOUNT = {
  cardNo: '6222020000000001',
  password: '123456',
  sessionId: 'atm_session_demo_001',
  customerName: '张三',
  accountNo: 'ACC10001',
  accountType: 'SAVING',
  accountStatus: 'NORMAL',
  balance: 5000
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
      customerName: MOCK_ACCOUNT.customerName,
      accountNo: MOCK_ACCOUNT.accountNo
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

  if (sessionId !== MOCK_ACCOUNT.sessionId) {
    throw createApiError('会话已失效，请重新登录', 401)
  }

  return {
    code: 200,
    message: '查询成功',
    data: {
      accountNo: MOCK_ACCOUNT.accountNo,
      balance: MOCK_ACCOUNT.balance
    }
  }
}

export async function mockGetProfile(sessionId) {
  await sleep(320)

  if (sessionId !== MOCK_ACCOUNT.sessionId) {
    throw createApiError('会话已失效，请重新登录', 401)
  }

  return {
    code: 200,
    message: '查询成功',
    data: {
      customerName: MOCK_ACCOUNT.customerName,
      cardNo: MOCK_ACCOUNT.cardNo,
      accountNo: MOCK_ACCOUNT.accountNo,
      accountType: MOCK_ACCOUNT.accountType,
      accountStatus: MOCK_ACCOUNT.accountStatus
    }
  }
}

