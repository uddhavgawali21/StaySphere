import { apiClient } from './client'

export const getMyPaymentAccount = () =>
  apiClient.get('/owner/payment-account').then((r) => r.data)

export const saveMyPaymentAccount = (payload) =>
  apiClient.put('/owner/payment-account', payload).then((r) => r.data)