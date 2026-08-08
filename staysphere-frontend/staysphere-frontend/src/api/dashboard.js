import { apiClient } from './client'

export const getOwnerDashboard = () =>
  apiClient.get('/owner/dashboard').then((r) => r.data)

export const getAdminUsers = (params) =>
  apiClient.get('/admin/users', { params }).then((r) => r.data)

export const updateUserStatus = (userId, accountStatus) =>
  apiClient.put(`/admin/users/${userId}/status`, { accountStatus }).then((r) => r.data)

export const resetUserPassword = (userId, newPassword) =>
  apiClient.put(`/admin/users/${userId}/reset-password`, { newPassword }).then((r) => r.data)

export const getAuditLogs = (params) =>
  apiClient.get('/admin/audit-logs', { params }).then((r) => r.data)

export const getAdminProperties = (params) =>
  apiClient.get('/admin/properties', { params }).then((r) => r.data)

export const updateAdminPropertyStatus = (propertyId, propertyStatus) =>
  apiClient.put(`/admin/properties/${propertyId}/status`, { propertyStatus }).then((r) => r.data)

export const getAdminBookings = (params) =>
  apiClient.get('/admin/bookings', { params }).then((r) => r.data)