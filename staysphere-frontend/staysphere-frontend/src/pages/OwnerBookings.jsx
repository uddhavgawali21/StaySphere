import { useEffect, useState } from 'react'
import { getPropertiesByOwner } from '../api/properties'
import { getBookingsByProperty, confirmBooking, rejectBooking } from '../api/bookings'
import { getTransactionsByBooking, recordOfflinePayment } from '../api/transactions'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'
import StatusBadge from '../components/StatusBadge.jsx'
import './MyBookings.css'

// Keep in sync with com.rms.enums.PaymentType — offline payments can be
// recorded against any of these components.
const OFFLINE_PAYMENT_TYPES = ['TOKEN', 'DEPOSIT', 'RENT']

export default function OwnerBookings() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [propertyTitles, setPropertyTitles] = useState({})
  const [transactionsByBooking, setTransactionsByBooking] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Offline-payment mini-form state, keyed by bookingId
  const [offlineDrafts, setOfflineDrafts] = useState({})
  const [recordingKey, setRecordingKey] = useState(null)

  useEffect(() => { load() }, [])

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
      const flat = perProperty.flat()
      setBookings(flat)

      const txnEntries = await Promise.all(
        flat.map(async (b) => [b.bookingId, await getTransactionsByBooking(b.bookingId).catch(() => [])])
      )
      setTransactionsByBooking(Object.fromEntries(txnEntries))
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
      setError(apiErrorMessage(err, 'Could not approve this booking.'))
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

  function updateDraft(bookingId, patch) {
    setOfflineDrafts((prev) => ({
      ...prev,
      [bookingId]: { paymentType: 'RENT', amount: '', notes: '', ...prev[bookingId], ...patch }
    }))
  }

  // Owner marks a payment the tenant made directly to them (cash/UPI) as
  // received. This is the ONLY way an offline payment gets recorded — a
  // tenant has no equivalent action, so they can never mark their own
  // offline payment as paid.
  async function handleRecordOffline(bookingId) {
    const draft = offlineDrafts[bookingId] || { paymentType: 'RENT', amount: '' }
    if (!draft.amount || Number(draft.amount) <= 0) {
      setError('Enter a valid amount before recording the payment.')
      return
    }
    setError('')
    setRecordingKey(bookingId)
    try {
      await recordOfflinePayment({
        bookingId,
        paymentType: draft.paymentType,
        amount: Number(draft.amount),
        notes: draft.notes || undefined
      })
      updateDraft(bookingId, { amount: '', notes: '' })
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not record this payment.'))
    } finally {
      setRecordingKey(null)
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
            const actionable = booking.bookingStatus === 'REQUESTED'
            const canRecordOffline = ['PAYMENT_PENDING', 'CONFIRMED'].includes(booking.bookingStatus)
              && booking.paymentStatus !== 'FULLY_PAID'
            const draft = offlineDrafts[booking.bookingId] || { paymentType: 'RENT', amount: '', notes: '' }
            const transactions = transactionsByBooking[booking.bookingId] || []

            return (
              <div key={booking.bookingId} className="booking-row">
                <div>
                  <p className="booking-meta" style={{ fontWeight: 500 }}>
                    {booking.propertyTitle || propertyTitles[booking.propertyId] || `Property #${booking.propertyId}`}
                  </p>
                  <p className="booking-meta">
                    Stay: {booking.startDate}{booking.endDate ? ` → ${booking.endDate}` : ' (open-ended)'}
                  </p>
                  <p className="booking-meta">
                    Tenant: <strong>{booking.tenantName || `#${booking.tenantId}`}</strong>
                    {booking.tenantEmail && (
                      <> · <a href={`mailto:${booking.tenantEmail}`} style={{ color: 'var(--brass)' }}>{booking.tenantEmail}</a></>
                    )}
                    {booking.tenantPhone && <> · {booking.tenantPhone}</>}
                  </p>

                  {/* Total / paid / pending — always backend-computed */}
                  <p className="booking-meta" style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 6 }}>
                    <StatusBadge status={booking.paymentStatus} />
                    <span>
                      Total ₹{Number(booking.totalPayable).toLocaleString('en-IN')} ·
                      {' '}Paid ₹{Number(booking.amountPaid).toLocaleString('en-IN')} ·
                      {' '}Pending ₹{Number(booking.amountPending).toLocaleString('en-IN')}
                    </span>
                  </p>

                  {canRecordOffline && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
                      <select
                        value={draft.paymentType}
                        onChange={(e) => updateDraft(booking.bookingId, { paymentType: e.target.value })}
                      >
                        {OFFLINE_PAYMENT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                      </select>
                      <input
                        type="number"
                        min="1"
                        placeholder="Amount received"
                        value={draft.amount}
                        onChange={(e) => updateDraft(booking.bookingId, { amount: e.target.value })}
                        style={{ width: 140 }}
                      />
                      <input
                        type="text"
                        placeholder="Note (optional)"
                        value={draft.notes}
                        onChange={(e) => updateDraft(booking.bookingId, { notes: e.target.value })}
                        style={{ width: 180 }}
                      />
                      <button
                        className="btn btn-brass btn-sm"
                        onClick={() => handleRecordOffline(booking.bookingId)}
                        disabled={recordingKey === booking.bookingId}
                      >
                        {recordingKey === booking.bookingId ? 'Recording…' : 'Mark offline payment received'}
                      </button>
                    </div>
                  )}

                  {transactions.length > 0 && (
                    <details style={{ marginTop: 10 }}>
                      <summary style={{ cursor: 'pointer', fontSize: '0.8rem' }}>Payment history</summary>
                      <div style={{ marginTop: 6, display: 'flex', flexDirection: 'column', gap: 4 }}>
                        {transactions.map((t) => (
                          <div key={t.transactionId} className="booking-meta" style={{ fontSize: '0.78rem' }}>
                            {t.paymentType} — ₹{Number(t.amount).toLocaleString('en-IN')} — {t.paymentStatus}
                            {t.paymentSource === 'OFFLINE' ? ` (recorded by ${t.recordedByOwnerName || 'you'}${t.notes ? `: ${t.notes}` : ''})` : ''}
                            {t.paymentDate ? ` — ${new Date(t.paymentDate).toLocaleString()}` : ''}
                          </div>
                        ))}
                      </div>
                    </details>
                  )}
                </div>
                <div className="booking-actions">
                  <StatusBadge status={booking.bookingStatus} />
                  {actionable && (
                    <>
                      <button className="btn btn-primary btn-sm" onClick={() => handleConfirm(booking.bookingId)}>
                        Approve
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleReject(booking.bookingId)}>
                        Reject
                      </button>
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