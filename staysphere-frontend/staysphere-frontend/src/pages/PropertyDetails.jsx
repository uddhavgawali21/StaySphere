import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getProperty, getFacilities, getPropertyImages } from '../api/properties'
import { createBooking } from '../api/bookings'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'
import StatusBadge from '../components/StatusBadge.jsx'
import './PropertyDetails.css'

export default function PropertyDetails() {
  const { propertyId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [property, setProperty] = useState(null)
  const [facilities, setFacilities] = useState([])
  const [images, setImages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [bookingError, setBookingError] = useState('')
  const [bookingSuccess, setBookingSuccess] = useState('')
  const [booking, setBooking] = useState(false)

  useEffect(() => {
    async function load() {
      setLoading(true)
      try {
        const [propertyData, facilityData, imageData] = await Promise.all([
          getProperty(propertyId),
          getFacilities(propertyId).catch(() => []),
          getPropertyImages(propertyId).catch(() => [])
        ])
        setProperty(propertyData)
        setFacilities(facilityData)
        setImages(imageData)
      } catch {
        setError('This property could not be found.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [propertyId])

  async function handleBook(e) {
    e.preventDefault()
    setBookingError('')
    setBookingSuccess('')

    if (!user) {
      navigate('/login')
      return
    }
    if (user.role !== 'TENANT') {
      setBookingError('Only tenant accounts can request a booking.')
      return
    }

    setBooking(true)
    try {
      const result = await createBooking({ propertyId: Number(propertyId), startDate, endDate: endDate || null })
      setBookingSuccess(`Booking requested — status: ${result.bookingStatus}. Track it under "My bookings".`)
    } catch (err) {
      setBookingError(apiErrorMessage(err, 'Could not create the booking.'))
    } finally {
      setBooking(false)
    }
  }

  if (loading) return <div className="spinner-row">Loading…</div>
  if (error) return <div className="container page"><div className="banner-error">{error}</div></div>
  if (!property) return null

  const primaryImage = images.find((img) => img.primary)

  return (
    <div className="page">
      <div className="container details-layout">
        <div>
          <div className="details-arch">
            {primaryImage ? (
              <img src={primaryImage.imageUrl} alt={property.title} />
            ) : (
              <span>{property.propertyType}</span>
            )}
          </div>

          <div className="details-header">
            <div>
              <h1>{property.title}</h1>
              <p className="details-location">{property.addressLine}, {property.area}, {property.city}, {property.state} — {property.pincode}</p>
            </div>
            <StatusBadge status={property.propertyStatus} />
          </div>

          <p>{property.description}</p>

          {facilities.length > 0 && (
            <div className="facility-list">
              {facilities.map((f) => (
                <span key={f.facilityId} className="facility-chip">{f.facilityName}</span>
              ))}
            </div>
          )}
        </div>

        <aside className="booking-panel">
          <div className="price-row">
            <span className="price">₹{Number(property.rentAmount).toLocaleString('en-IN')}</span>
            <span className="price-unit">/month</span>
          </div>
          <p className="deposit-line">Deposit: ₹{Number(property.depositAmount).toLocaleString('en-IN')}</p>
          <p className="deposit-line">{property.occupancyType} occupancy · {property.propertyType}</p>

          <hr />

          <form onSubmit={handleBook}>
            {bookingError && <div className="banner-error">{bookingError}</div>}
            {bookingSuccess && (
              <div className="banner-error" style={{ background: 'var(--sage-dim)', color: 'var(--sage)' }}>
                {bookingSuccess}
              </div>
            )}
            <div className="field">
              <label>Start date</label>
              <input
                type="date"
                required
                value={startDate}
                min={new Date(Date.now() + 86400000).toISOString().slice(0, 10)}
                onChange={(e) => {
                  setStartDate(e.target.value)
                  // Keep end date valid if it's now before the new start date
                  if (endDate && endDate <= e.target.value) {
                    setEndDate('')
                  }
                }}
              />
            </div>
            <div className="field">
              <label>End date <span className="deposit-line" style={{ display: 'inline' }}>(optional)</span></label>
              <input
                type="date"
                value={endDate}
                disabled={!startDate}
                min={
                  startDate
                    ? new Date(new Date(startDate).getTime() + 86400000).toISOString().slice(0, 10)
                    : undefined
                }
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
            <button className="btn btn-primary" type="submit" disabled={booking} style={{ width: '100%' }}>
              {booking ? 'Requesting…' : 'Request booking'}
            </button>
          </form>
        </aside>
      </div>
    </div>
  )
}