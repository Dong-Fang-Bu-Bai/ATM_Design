import { defineStore } from 'pinia'

const STORAGE_KEY = 'atm-front-session'

export const useSessionStore = defineStore('session', {
  state: () => ({
    sessionId: '',
    customerName: '',
    accountNo: '',
    profile: null,
    balance: null,
    hydrated: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.sessionId),
    maskedAccountNo: (state) => {
      if (!state.accountNo) {
        return '--'
      }

      return `${state.accountNo.slice(0, 3)}****${state.accountNo.slice(-3)}`
    }
  },
  actions: {
    hydrateFromStorage() {
      if (this.hydrated || typeof window === 'undefined') {
        return
      }

      const raw = window.localStorage.getItem(STORAGE_KEY)

      if (raw) {
        const saved = JSON.parse(raw)
        this.sessionId = saved.sessionId || ''
        this.customerName = saved.customerName || ''
        this.accountNo = saved.accountNo || ''
        this.profile = saved.profile || null
        this.balance = saved.balance ?? null
      }

      this.hydrated = true
    },
    persist() {
      if (typeof window === 'undefined') {
        return
      }

      window.localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          sessionId: this.sessionId,
          customerName: this.customerName,
          accountNo: this.accountNo,
          profile: this.profile,
          balance: this.balance
        })
      )
    },
    setSession(data) {
      this.sessionId = data.sessionId
      this.customerName = data.customerName
      this.accountNo = data.accountNo
      this.persist()
    },
    setProfile(profile) {
      this.profile = profile
      this.persist()
    },
    setBalance(balance) {
      this.balance = balance
      this.persist()
    },
    clearSession() {
      this.sessionId = ''
      this.customerName = ''
      this.accountNo = ''
      this.profile = null
      this.balance = null

      if (typeof window !== 'undefined') {
        window.localStorage.removeItem(STORAGE_KEY)
      }
    }
  }
})

