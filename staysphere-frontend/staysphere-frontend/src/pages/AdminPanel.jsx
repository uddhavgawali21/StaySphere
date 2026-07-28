import { useEffect, useState } from 'react'
import {
  getAdminUsers, updateUserStatus,
  getAdminProperties, updateAdminPropertyStatus,
  getAdminBookings
} from '../api/dashboard'
import StatusBadge from '../components/StatusBadge.jsx'
import './AdminPanel.css'

const TABS = ['Users', 'Properties', 'Bookings']

export default function AdminPanel() {
  const [tab, setTab] = useState('Users')

  return (
    <div className="page">
      <div className="container">
        <h1>Admin</h1>
        <div className="admin-tabs">
          {TABS.map((t) => (
            <button
              key={t}
              className={`admin-tab ${tab === t ? 'admin-tab-active' : ''}`}
              onClick={() => setTab(t)}
            >
              {t}
            </button>
          ))}
        </div>

        {tab === 'Users' && <UsersTab />}
        {tab === 'Properties' && <PropertiesTab />}
        {tab === 'Bookings' && <BookingsTab />}
      </div>
    </div>
  )
}

function UsersTab() {
  const [filters, setFilters] = useState({ role: '', accountStatus: '' })
  const [data, setData] = useState({ content: [] })
  const [error, setError] = useState('')

  useEffect(() => { load() }, [filters])

  function load() {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v))
    getAdminUsers(params).then(setData).catch(() => setError('Could not load users.'))
  }

  async function handleStatusChange(userId, accountStatus) {
    try {
      await updateUserStatus(userId, accountStatus)
      load()
    } catch {
      setError('Could not update that user.')
    }
  }

  return (
    <div>
      <div className="admin-filters">
        <select value={filters.role} onChange={(e) => setFilters({ ...filters, role: e.target.value })}>
          <option value="">Any role</option>
          <option value="TENANT">Tenant</option>
          <option value="OWNER">Owner</option>
          <option value="ADMIN">Admin</option>
        </select>
        <select value={filters.accountStatus} onChange={(e) => setFilters({ ...filters, accountStatus: e.target.value })}>
          <option value="">Any status</option>
          <option value="ACTIVE">Active</option>
          <option value="SUSPENDED">Suspended</option>
          <option value="DEACTIVATED">Deactivated</option>
        </select>
      </div>
      {error && <div className="banner-error">{error}</div>}

      <table className="admin-table">
        <thead>
          <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          {data.content.map((u) => (
            <tr key={u.userId}>
              <td>{u.firstName} {u.lastName}</td>
              <td>{u.email}</td>
              <td>{u.role}</td>
              <td><StatusBadge status={u.accountStatus} /></td>
              <td>
                <select
                  className="row-select"
                  value={u.accountStatus}
                  onChange={(e) => handleStatusChange(u.userId, e.target.value)}
                >
                  <option value="ACTIVE">Active</option>
                  <option value="SUSPENDED">Suspended</option>
                  <option value="DEACTIVATED">Deactivated</option>
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {data.content.length === 0 && <p className="booking-meta" style={{ padding: '16px 0' }}>No users match those filters.</p>}
    </div>
  )
}

function PropertiesTab() {
  const [filters, setFilters] = useState({ city: '', propertyStatus: '' })
  const [data, setData] = useState({ content: [] })
  const [error, setError] = useState('')

  useEffect(() => { load() }, [filters])

  function load() {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v))
    getAdminProperties(params).then(setData).catch(() => setError('Could not load properties.'))
  }

  async function handleStatusChange(propertyId, propertyStatus) {
    try {
      await updateAdminPropertyStatus(propertyId, propertyStatus)
      load()
    } catch {
      setError('Could not update that property.')
    }
  }

  return (
    <div>
      <div className="admin-filters">
        <input
          placeholder="Filter by city"
          value={filters.city}
          onChange={(e) => setFilters({ ...filters, city: e.target.value })}
        />
        <select value={filters.propertyStatus} onChange={(e) => setFilters({ ...filters, propertyStatus: e.target.value })}>
          <option value="">Any status</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </div>
      {error && <div className="banner-error">{error}</div>}

      <table className="admin-table">
        <thead>
          <tr><th>Title</th><th>City</th><th>Rent</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          {data.content.map((p) => (
            <tr key={p.propertyId}>
              <td>{p.title}</td>
              <td>{p.city}</td>
              <td>₹{Number(p.rentAmount).toLocaleString('en-IN')}</td>
              <td><StatusBadge status={p.propertyStatus} /></td>
              <td>
                <select
                  className="row-select"
                  value={p.propertyStatus}
                  onChange={(e) => handleStatusChange(p.propertyId, e.target.value)}
                >
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {data.content.length === 0 && <p className="booking-meta" style={{ padding: '16px 0' }}>No properties match those filters.</p>}
    </div>
  )
}

function BookingsTab() {
  const [bookingStatus, setBookingStatus] = useState('')
  const [data, setData] = useState({ content: [] })
  const [error, setError] = useState('')

  useEffect(() => {
    const params = bookingStatus ? { bookingStatus } : {}
    getAdminBookings(params).then(setData).catch(() => setError('Could not load bookings.'))
  }, [bookingStatus])

  return (
    <div>
      <div className="admin-filters">
        <select value={bookingStatus} onChange={(e) => setBookingStatus(e.target.value)}>
          <option value="">Any status</option>
          <option value="REQUESTED">Requested</option>
          <option value="PAYMENT_PENDING">Payment pending</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="REJECTED">Rejected</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>
      {error && <div className="banner-error">{error}</div>}

      <table className="admin-table">
        <thead>
          <tr><th>Booking</th><th>Property</th><th>Tenant</th><th>Move-in</th><th>Status</th></tr>
        </thead>
        <tbody>
          {data.content.map((b) => (
            <tr key={b.bookingId}>
              <td>#{b.bookingId}</td>
              <td>#{b.propertyId}</td>
              <td>#{b.tenantId}</td>
              <td>{b.moveInDate}</td>
              <td><StatusBadge status={b.bookingStatus} /></td>
            </tr>
          ))}
        </tbody>
      </table>
      {data.content.length === 0 && <p className="booking-meta" style={{ padding: '16px 0' }}>No bookings match that filter.</p>}
    </div>
  )
}
