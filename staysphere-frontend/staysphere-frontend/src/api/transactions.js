import { apiClient } from './client'

export const createTransaction = (payload) =>
  apiClient.post('/transactions', payload).then((r) => r.data)

export const getTransactionsByBooking = (bookingId) =>
  apiClient.get(`/transactions/booking/${bookingId}`).then((r) => r.data)

export const updateTransactionStatus = (transactionId, paymentStatus) =>
  apiClient.put(`/transactions/${transactionId}/status`, { paymentStatus }).then((r) => r.data)
