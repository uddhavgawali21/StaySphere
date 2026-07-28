import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const apiClient = axios.create({
  baseURL: BASE_URL
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('staysphere_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('staysphere_token')
      localStorage.removeItem('staysphere_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Pulls the backend's { message: "..." } shape out of an error, with a fallback.
export function apiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  return error?.response?.data?.message || error?.response?.data?.errors
    ? formatValidationErrors(error.response.data) || fallback
    : fallback
}

function formatValidationErrors(data) {
  if (data.message) return data.message
  if (data.errors) return Object.values(data.errors).join(', ')
  return null
}
