import { apiClient } from './client'

// NOTE: the backend's /properties/search currently ignores query params and
// just returns all ACTIVE properties (same as GET /properties) — no server-side
// filtering or pagination. Filtering/pagination is done client-side in Home.jsx
// until that endpoint is restored to take real filters. Swap this back to
// passing { params } once it does.
export const searchProperties = () =>
  apiClient.get('/properties/search').then((r) => r.data)

export const getProperty = (propertyId) =>
  apiClient.get(`/properties/${propertyId}`).then((r) => r.data)

export const getPropertiesByOwner = (ownerId) =>
  apiClient.get(`/properties/owner/${ownerId}`).then((r) => r.data)

export const createProperty = (payload) =>
  apiClient.post('/properties', payload).then((r) => r.data)

export const updateProperty = (propertyId, payload) =>
  apiClient.put(`/properties/${propertyId}`, payload).then((r) => r.data)

export const deleteProperty = (propertyId) =>
  apiClient.delete(`/properties/${propertyId}`).then((r) => r.data)

export const getPropertyImages = (propertyId) =>
  apiClient.get(`/properties/${propertyId}/images`).then((r) => r.data)

export const addPropertyImage = (propertyId, payload) =>
  apiClient.post(`/properties/${propertyId}/images`, payload).then((r) => r.data)

export const setPrimaryImage = (imageId) =>
  apiClient.put(`/properties/images/${imageId}/primary`).then((r) => r.data)

export const deletePropertyImage = (imageId) =>
  apiClient.delete(`/properties/images/${imageId}`).then((r) => r.data)

export const getFacilities = (propertyId) =>
  apiClient.get(`/properties/${propertyId}/facilities`).then((r) => r.data)

export const addFacility = (propertyId, facilityName) =>
  apiClient.post(`/properties/${propertyId}/facilities`, { facilityName }).then((r) => r.data)

export const deleteFacility = (facilityId) =>
  apiClient.delete(`/properties/facilities/${facilityId}`).then((r) => r.data)