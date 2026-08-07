import { apiClient } from './client'

export const checkoutTransaction = (payload) =>
  apiClient.post('/transactions/checkout', payload).then((r) => r.data)

export const verifyTransaction = (transactionId, payload) =>
  apiClient.post(`/transactions/${transactionId}/verify`, payload).then((r) => r.data)

export const getTransactionsByBooking = (bookingId) =>
  apiClient.get(`/transactions/booking/${bookingId}`).then((r) => r.data)