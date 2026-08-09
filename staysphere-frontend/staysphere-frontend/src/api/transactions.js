import { apiClient } from './client'

// payload: { bookingId, paymentType, amount? }
// amount is only used for paymentType 'RENT' (partial/installment payment).
// Omit it to pay the full remaining rent in one go.
export const checkoutTransaction = (payload) =>
  apiClient.post('/transactions/checkout', payload).then((r) => r.data)

export const verifyTransaction = (transactionId, payload) =>
  apiClient.post(`/transactions/${transactionId}/verify`, payload).then((r) => r.data)

export const getTransactionsByBooking = (bookingId) =>
  apiClient.get(`/transactions/booking/${bookingId}`).then((r) => r.data)

// Owner-only — records a payment the tenant made directly to them.
// payload: { bookingId, paymentType, amount, notes? }
export const recordOfflinePayment = (payload) =>
  apiClient.post('/transactions/offline', payload).then((r) => r.data)