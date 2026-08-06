import { useEffect, useState } from 'react'
import { getPropertiesByOwner } from '../api/properties'
import { getBookingsByProperty, confirmBooking, rejectBooking } from '../api/bookings'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'
import StatusBadge from '../components/StatusBadge.jsx'
import './MyBookings.css'

export default function OwnerBookings() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [propertyTitles, setPropertyTitles] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    load()
  }, [])

  async function load() {
    setLoading(true)
    setError('')
    try {
      const properties = await getPropertiesByOwner(user.userId)
      const titleMap = Object.fromEntries(properties.map((p) => [p.propertyId, p.title]))
      setPropertyTitles(titleMap)

      const perProperty = await Promise.all(
        properties.map((p) => getBookingsByProperty(p.propertyId))
      )
      setBookings(perProperty.flat())
    } catch {
      setError('Could not load bookings for your properties.')
    } finally {
      setLoading(false)
    }
  }

  async function handleConfirm(bookingId) {
    try {
      await confirmBooking(bookingId)
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not confirm this booking.'))
    }
  }

  async function handleReject(bookingId) {
    try {
      await rejectBooking(bookingId)
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not reject this booking.'))
    }
  }

  if (loading) return <div className="spinner-row">Loading bookings…</div>

  return (
    <div className="page">
      <div className="container">
        <h1>Booking requests</h1>
        {error && <div className="banner-error">{error}</div>}

        {bookings.length === 0 && (
          <div className="empty-state">
            <h3>No booking requests</h3>
            <p>Requests for your properties will show up here.</p>
          </div>
        )}

        <div className="booking-list">
          {bookings.map((booking) => {
            const canConfirm = booking.bookingStatus === 'REQUESTED'
            const canReject = ['REQUESTED', 'PAYMENT_PENDING'].includes(booking.bookingStatus)
            return (
              <div key={booking.bookingId} className="booking-row">
                <div>
                  <p className="booking-meta">{propertyTitles[booking.propertyId] || `Property #${booking.propertyId}`}</p>
                  <p className="booking-meta">Move-in: {booking.moveInDate} · Tenant #{booking.tenantId}</p>
                </div>
                <div className="booking-actions">
                  <StatusBadge status={booking.bookingStatus} />
                  {(canConfirm || canReject) && (
                    <>
                      {canConfirm && (
                        <button className="btn btn-primary btn-sm" onClick={() => handleConfirm(booking.bookingId)}>
                          Confirm
                        </button>
                      )}
                      {canReject && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleReject(booking.bookingId)}>
                          Reject
                        </button>
                      )}
                    </>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
