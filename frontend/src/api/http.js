import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 8000
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data

    if (payload && typeof payload === 'object' && 'code' in payload && payload.code !== 200) {
      const error = new Error(payload.message || '请求失败，请稍后重试')
      error.response = {
        ...response,
        data: payload
      }
      return Promise.reject(error)
    }

    return response
  },
  (error) => Promise.reject(error)
)

export function getErrorMessage(error, fallback = '请求失败，请稍后重试') {
  return error?.response?.data?.message || error?.message || fallback
}

export default http

