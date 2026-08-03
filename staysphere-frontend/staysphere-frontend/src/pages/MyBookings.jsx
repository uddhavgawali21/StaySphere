import { useEffect, useState } from 'react'
import { getBookingsByTenant, cancelBooking } from '../api/bookings'
import { createTransaction, getTransactionsByBooking, updateTransactionStatus } from '../api/transactions'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'
import StatusBadge from '../components/StatusBadge.jsx'
import './MyBookings.css'

export default function MyBookings() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [transactionsByBooking, setTransactionsByBooking] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [payForm, setPayForm] = useState({}) // bookingId -> { amount, paymentMethod }

  useEffect(() => {
    load()
  }, [])

  async function load() {
    setLoading(true)
    setError('')
    try {
      const data = await getBookingsByTenant(user.userId)
      setBookings(data)
      const entries = await Promise.all(
        data.map(async (b) => [b.bookingId, await getTransactionsByBooking(b.bookingId).catch(() => [])])
      )
      setTransactionsByBooking(Object.fromEntries(entries))
    } catch {
      setError('Could not load your bookings.')
    } finally {
      setLoading(false)
    }
  }

  async function handleCancel(bookingId) {
    setActionError('')
    try {
      await cancelBooking(bookingId)
      load()
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Could not cancel this booking.'))
    }
  }

  function updatePayForm(bookingId, field, value) {
    setPayForm({ ...payForm, [bookingId]: { ...payForm[bookingId], [field]: value } })
  }

  async function handlePay(bookingId) {
    setActionError('')
    const details = payForm[bookingId] || {}
    if (!details.amount || !details.paymentMethod) {
      setActionError('Enter an amount and choose a payment method first.')
      return
    }
    try {
      await createTransaction({
        bookingId,
        transactionRef: `TXN-${bookingId}-${Date.now()}`,
        amount: Number(details.amount),
        paymentMethod: details.paymentMethod
      })
      load()
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Could not start the payment.'))
    }
  }

  async function handleSimulate(transactionId, status) {
    setActionError('')
    try {
      await updateTransactionStatus(transactionId, status)
      load()
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Could not update the payment.'))
    }
  }

  if (loading) return <div className="spinner-row">Loading your bookings…</div>

  return (
    <div className="page">
      <div className="container">
        <h1>My bookings</h1>
        {error && <div className="banner-error">{error}</div>}
        {actionError && <div className="banner-error">{actionError}</div>}

        {bookings.length === 0 && (
          <div className="empty-state">
            <h3>No bookings yet</h3>
            <p>Browse listings and request a booking to see it here.</p>
          </div>
        )}

        <div className="booking-list">
          {bookings.map((booking) => {
            const transactions = transactionsByBooking[booking.bookingId] || []
            const latestTransaction = transactions[transactions.length - 1]
            const canPay = booking.bookingStatus === 'PAYMENT_PENDING' && !latestTransaction
            return (
              <div key={booking.bookingId} className="booking-row">
                <div>
                  <p className="booking-meta">Booking #{booking.bookingId} · Property #{booking.propertyId}</p>
                  <p className="booking-meta">Move-in: {booking.moveInDate}</p>
                  {latestTransaction && (
                    <p className="booking-meta">
                      Payment ref {latestTransaction.transactionRef} — <StatusBadge status={latestTransaction.paymentStatus} />
                    </p>
                  )}
                </div>

                <div className="booking-actions">
                  <StatusBadge status={booking.bookingStatus} />

                  {canPay && (
                    <div className="pay-form">
                      <input
                        type="number"
                        placeholder="Amount ₹"
                        value={payForm[booking.bookingId]?.amount || ''}
                        onChange={(e) => updatePayForm(booking.bookingId, 'amount', e.target.value)}
                      />
                      <select
                        value={payForm[booking.bookingId]?.paymentMethod || ''}
                        onChange={(e) => updatePayForm(booking.bookingId, 'paymentMethod', e.target.value)}
                      >
                        <option value="">Method</option>
                        <option value="UPI">UPI</option>
                        <option value="CARD">Card</option>
                        <option value="NETBANKING">Netbanking</option>
                        <option value="CASH">Cash</option>
                        <option value="BANK_TRANSFER">Bank transfer</option>
                      </select>
                      <button className="btn btn-brass btn-sm" onClick={() => handlePay(booking.bookingId)}>
                        Pay now
                      </button>
                    </div>
                  )}

                  {latestTransaction?.paymentStatus === 'PENDING' && (
                    <div className="pay-form">
                      <button className="btn btn-outline btn-sm" onClick={() => handleSimulate(latestTransaction.transactionId, 'SUCCESS')}>
                        Mark payment successful
                      </button>
                      <button className="btn btn-outline btn-sm" onClick={() => handleSimulate(latestTransaction.transactionId, 'FAILED')}>
                        Mark payment failed
                      </button>
                    </div>
                  )}

                  {canCancel && (
                    <button className="btn btn-danger btn-sm" onClick={() => handleCancel(booking.bookingId)}>
                      Cancel booking
                    </button>
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
