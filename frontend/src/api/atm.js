import http from './http'
import {
  mockChangePassword,
  mockDeposit,
  mockGetBalance,
  mockGetProfile,
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

