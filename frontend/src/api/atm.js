import http from './http'
import {
  mockCheckCashAvailability,
  mockChangePassword,
  mockDeposit,
  mockGetDeviceStatus,
  mockGetReceipt,
  mockGetBalance,
  mockGetProfile,
  mockGetTransactionHistory,
  mockLogin,
  mockLogout,
  mockTransfer,
  mockValidateTransactionSession,
  mockWithdraw
} from './mock'

const useMock = import.meta.env.VITE_USE_MOCK === 'true'

export async function login(payload) {
  if (useMock) {
    return mockLogin(payload)
  }

  const { data } = await http.post('/api/atm/auth/login', payload)
  return data
}

export async function logout(sessionId) {
  if (useMock) {
    return mockLogout(sessionId)
  }

  const { data } = await http.post('/api/atm/auth/logout', { sessionId })
  return data
}

export async function getBalance(sessionId) {
  if (useMock) {
    return mockGetBalance(sessionId)
  }

  const { data } = await http.get('/api/atm/accounts/balance', {
    params: { sessionId }
  })
  return data
}

export async function getProfile(sessionId) {
  if (useMock) {
    return mockGetProfile(sessionId)
  }

  const { data } = await http.get('/api/atm/accounts/full-info', {
    params: { sessionId }
  })
  return data
}

export async function validateTransactionSession(sessionId) {
  if (useMock) {
    return mockValidateTransactionSession(sessionId)
  }

  const { data } = await http.get('/api/atm/accounts/validate-transaction', {
    params: { sessionId }
  })
  return data
}

export async function withdraw(payload) {
  if (useMock) {
    return mockWithdraw(payload)
  }

  const { data } = await http.post('/api/atm/transactions/withdraw', payload)
  return data
}

export async function deposit(payload) {
  if (useMock) {
    return mockDeposit(payload)
  }

  const { data } = await http.post('/api/atm/transactions/deposit', payload)
  return data
}

export async function transfer(payload) {
  if (useMock) {
    return mockTransfer(payload)
  }

  const { data } = await http.post('/api/atm/transactions/transfer', payload)
  return data
}

export async function changePassword(payload) {
  if (useMock) {
    return mockChangePassword(payload)
  }

  const { data } = await http.post('/api/atm/auth/change-password', payload)
  return data
}

export async function getTransactionHistory(sessionId, params = {}) {
  if (useMock) {
    return mockGetTransactionHistory(sessionId, params)
  }

  const { data } = await http.get('/api/atm/transactions/history', {
    params: {
      sessionId,
      page: params.page,
      size: params.size
    }
  })
  return data
}

export async function getReceipt(transactionId, sessionId) {
  if (useMock) {
    return mockGetReceipt(transactionId, sessionId)
  }

  const { data } = await http.get(`/api/atm/receipts/${encodeURIComponent(transactionId)}`, {
    params: { sessionId }
  })
  return data
}

export async function getDeviceStatus() {
  if (useMock) {
    return mockGetDeviceStatus()
  }

  const { data } = await http.get('/api/atm/device/status')
  return data
}

export async function checkCashAvailability(payload) {
  const body = typeof payload === 'object' ? payload : { amount: payload }

  if (useMock) {
    return mockCheckCashAvailability(body)
  }

  const { data } = await http.post('/api/atm/device/cash-check', body)
  return data
}

