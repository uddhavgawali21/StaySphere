import { apiClient } from './client'

export const registerUser = (payload) => apiClient.post('/users/register', payload).then((r) => r.data)

export const loginUser = (payload) => apiClient.post('/users/login', payload).then((r) => r.data)
