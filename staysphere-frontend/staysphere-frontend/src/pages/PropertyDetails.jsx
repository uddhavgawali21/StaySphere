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
  const [activeImageIndex, setActiveImageIndex] = useState(0)

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
        const orderedImages = [...imageData].sort((a, b) => (b.primary === true) - (a.primary === true))
        setImages(orderedImages)
        setActiveImageIndex(0)
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

    if (!user) { navigate('/login'); return }
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

  const activeImage = images[activeImageIndex]
  const isMultiRoom = property.totalRooms > 1
  const noRoomsLeft = isMultiRoom && property.availableRooms <= 0
  const propertyUnavailable = property.propertyStatus !== 'ACTIVE' || noRoomsLeft

  function showPrevImage() { setActiveImageIndex((i) => (i - 1 + images.length) % images.length) }
  function showNextImage() { setActiveImageIndex((i) => (i + 1) % images.length) }

  return (
    <div className="page">
      <div className="container details-layout">
        <div>
          <div className="details-gallery">
            <div className="details-arch">
              {activeImage ? (
                <img src={activeImage.imageUrl} alt={`${property.title} — photo ${activeImageIndex + 1}`} />
              ) : (
                <span>{property.propertyType}</span>
              )}
              {images.length > 1 && (
                <>
                  <button type="button" className="gallery-nav gallery-nav-prev" onClick={showPrevImage} aria-label="Previous photo">‹</button>
                  <button type="button" className="gallery-nav gallery-nav-next" onClick={showNextImage} aria-label="Next photo">›</button>
                  <span className="gallery-count">{activeImageIndex + 1} / {images.length}</span>
                </>
              )}
            </div>
            {images.length > 1 && (
              <div className="gallery-thumbs">
                {images.map((img, index) => (
                  <button
                    type="button"
                    key={img.imageId ?? index}
                    className={`gallery-thumb ${index === activeImageIndex ? 'gallery-thumb-active' : ''}`}
                    onClick={() => setActiveImageIndex(index)}
                  >
                    <img src={img.imageUrl} alt={`${property.title} thumbnail ${index + 1}`} />
                  </button>
                ))}
              </div>
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
          {/* FIX: show available rooms for multi-room properties */}
          {property.totalRooms > 1 && (
            <p className="deposit-line" style={{ color: property.availableRooms > 0 ? 'var(--sage)' : 'var(--rust)' }}>
              {property.availableRooms} of {property.totalRooms} rooms available
            </p>
          )}

          <hr />

          {propertyUnavailable ? (
            <div className="banner-error">
              {noRoomsLeft
                ? 'All rooms are currently booked for this property.'
                : 'This property is not currently available for booking.'}
            </div>
          ) : (
            <form onSubmit={handleBook}>
              {bookingError && <div className="banner-error">{bookingError}</div>}
              {/* FIX: use banner-success class — was using banner-error with inline style hack */}
              {bookingSuccess && <div className="banner-success">{bookingSuccess}</div>}

              <div className="field">
                <label>Start date</label>
                <input
                  type="date"
                  required
                  value={startDate}
                  min={new Date(Date.now() + 86400000).toISOString().slice(0, 10)}
                  onChange={(e) => {
                    setStartDate(e.target.value)
                    if (endDate && endDate <= e.target.value) setEndDate('')
                  }}
                />
              </div>
              <div className="field">
                <label>End date <span className="deposit-line" style={{ display: 'inline' }}>(optional)</span></label>
                <input
                  type="date"
                  value={endDate}
                  disabled={!startDate}
                  min={startDate ? new Date(new Date(startDate).getTime() + 86400000).toISOString().slice(0, 10) : undefined}
                  onChange={(e) => setEndDate(e.target.value)}
                />
              </div>
              <button className="btn btn-primary" type="submit" disabled={booking} style={{ width: '100%' }}>
                {booking ? 'Requesting…' : 'Request booking'}
              </button>
            </form>
          )}
        </aside>
      </div>
    </div>
  )
}