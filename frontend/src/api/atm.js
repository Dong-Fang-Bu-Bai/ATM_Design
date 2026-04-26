import http from './http'
import {
  mockGetBalance,
  mockGetProfile,
  mockLogin,
  mockLogout
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

  const { data } = await http.get('/api/atm/accounts/profile', {
    params: { sessionId }
  })
  return data
}

