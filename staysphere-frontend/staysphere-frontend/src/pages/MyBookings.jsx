import { useEffect, useState } from 'react'
import { getBookingsByTenant, cancelBooking } from '../api/bookings'
import { checkoutTransaction, verifyTransaction, getTransactionsByBooking } from '../api/transactions'
import { getProperty } from '../api/properties'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'
import StatusBadge from '../components/StatusBadge.jsx'
import './MyBookings.css'

export default function MyBookings() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [transactionsByBooking, setTransactionsByBooking] = useState({})
  const [propertiesById, setPropertiesById] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [payForm, setPayForm] = useState({}) // bookingId -> { paymentMethod }

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

      const uniquePropertyIds = [...new Set(data.map((b) => b.propertyId))]
      const properties = await Promise.all(uniquePropertyIds.map((id) => getProperty(id).catch(() => null)))
      setPropertiesById(Object.fromEntries(
        uniquePropertyIds.map((id, i) => [id, properties[i]]).filter(([, p]) => p)
      ))
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

  async function handlePay(bookingId, rentAmount) {
    setActionError('')
    const details = payForm[bookingId] || {}
    if (!details.paymentMethod) {
      setActionError('Choose a payment method first.')
      return
    }
    try {
      const checkout = await checkoutTransaction({
        bookingId,
        transactionRef: `TXN-${bookingId}-${Date.now()}`,
        amount: rentAmount,
        paymentMethod: details.paymentMethod
      })
      openRazorpay(checkout)
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Could not start the payment.'))
    }
  }

  function openRazorpay(checkout) {
    if (!window.Razorpay) {
      setActionError('Payment widget failed to load. Check your connection and try again.')
      return
    }
    const rzp = new window.Razorpay({
      key: checkout.razorpayKeyId,
      amount: checkout.amountInPaise,
      currency: checkout.currency,
      order_id: checkout.razorpayOrderId,
      name: 'StaySphere',
      // Explicitly request all methods, incl. UPI — Razorpay Checkout otherwise
      // only shows what's enabled on the merchant account, and UPI is off by
      // default on some fresh Test Mode accounts.
      method: {
        upi: true,
        card: true,
        netbanking: true,
        wallet: true
      },
      handler: async (response) => {
        try {
          await verifyTransaction(checkout.transactionId, {
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature
          })
        } catch (err) {
          setActionError(apiErrorMessage(err, 'Payment succeeded but verification failed.'))
        } finally {
          load()
        }
      },
      modal: {
        ondismiss: () => load()
      }
    })
    rzp.open()
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
            const canPay = (booking.bookingStatus === 'PAYMENT_PENDING' && !latestTransaction) ||
              (booking.bookingStatus === 'REQUESTED' && latestTransaction?.paymentStatus === 'FAILED')
            const canCancel = ['REQUESTED', 'PAYMENT_PENDING'].includes(booking.bookingStatus)
            const property = propertiesById[booking.propertyId]

            return (
              <div key={booking.bookingId} className="booking-row">
                <div>
                  <p className="booking-meta">Booking #{booking.bookingId} · Property #{booking.propertyId}</p>
                  <p className="booking-meta">Stay: {booking.startDate} → {booking.endDate}</p>
                  {latestTransaction && (
                    <p className="booking-meta">
                      Payment ref {latestTransaction.transactionRef} — <StatusBadge status={latestTransaction.paymentStatus} />
                    </p>
                  )}
                </div>

                <div className="booking-actions">
                  <StatusBadge status={booking.bookingStatus} />

                  {canPay && property && (
                    <div className="pay-form">
                      <span className="booking-meta">Amount: ₹{property.rentAmount}</span>
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
                      <button className="btn btn-brass btn-sm" onClick={() => handlePay(booking.bookingId, property.rentAmount)}>
                        Pay now
                      </button>
                    </div>
                  )}

                  {latestTransaction?.paymentStatus === 'PENDING' && (
                    <p className="booking-meta">Payment in progress — complete it in the Razorpay window, or refresh to retry.</p>
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