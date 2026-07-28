import { useEffect, useState } from 'react'
import { getOwnerDashboard } from '../api/dashboard'
import './OwnerDashboard.css'

export default function OwnerDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getOwnerDashboard()
      .then(setData)
      .catch(() => setError('Could not load your dashboard.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="spinner-row">Loading dashboard…</div>
  if (error) return <div className="container page"><div className="banner-error">{error}</div></div>
  if (!data) return null

  return (
    <div className="page">
      <div className="container">
        <h1>Owner dashboard</h1>

        <div className="stat-row">
          <StatTile label="Properties" value={data.totalProperties} />
          <StatTile label="Total bookings" value={data.totalBookings} />
          <StatTile label="Confirmed" value={data.confirmedBookings} accent="sage" />
          <StatTile label="Pending" value={data.pendingBookings} accent="brass" />
          <StatTile label="Earnings" value={`₹${Number(data.totalEarnings).toLocaleString('en-IN')}`} accent="ink" wide />
        </div>

        <h2 style={{ marginTop: 40 }}>By property</h2>
        <div className="property-stat-table">
          <div className="property-stat-header">
            <span>Property</span>
            <span>Bookings</span>
            <span>Confirmed</span>
            <span>Pending</span>
            <span>Earnings</span>
          </div>
          {data.properties.map((p) => (
            <div key={p.propertyId} className="property-stat-row">
              <span>{p.title}</span>
              <span>{p.totalBookings}</span>
              <span>{p.confirmedBookings}</span>
              <span>{p.pendingBookings}</span>
              <span>₹{Number(p.earnings).toLocaleString('en-IN')}</span>
            </div>
          ))}
          {data.properties.length === 0 && (
            <p className="booking-meta" style={{ padding: '18px 0' }}>No properties listed yet.</p>
          )}
        </div>
      </div>
    </div>
  )
}

function StatTile({ label, value, accent = 'ink', wide }) {
  return (
    <div className={`stat-tile stat-${accent}${wide ? ' stat-wide' : ''}`}>
      <span className="stat-value">{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  )
}
