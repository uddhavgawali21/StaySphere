import { apiClient } from './client'

export const createBooking = (payload) =>
  apiClient.post('/bookings', payload).then((r) => r.data)

export const getBooking = (bookingId) =>
  apiClient.get(`/bookings/${bookingId}`).then((r) => r.data)

export const getBookingsByTenant = (tenantId) =>
  apiClient.get(`/bookings/tenant/${tenantId}`).then((r) => r.data)

export const getBookingsByProperty = (propertyId) =>
  apiClient.get(`/bookings/property/${propertyId}`).then((r) => r.data)

export const confirmBooking = (bookingId) =>
  apiClient.put(`/bookings/${bookingId}/confirm`).then((r) => r.data)

export const rejectBooking = (bookingId) =>
  apiClient.put(`/bookings/${bookingId}/reject`).then((r) => r.data)

export const cancelBooking = (bookingId) =>
  apiClient.put(`/bookings/${bookingId}/cancel`).then((r) => r.data)
