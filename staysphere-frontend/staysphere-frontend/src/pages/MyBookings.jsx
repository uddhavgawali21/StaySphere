import { useEffect, useState } from 'react'
import { getBookingsByTenant, cancelBooking } from '../api/bookings'
import { checkoutTransaction, verifyTransaction, getTransactionsByBooking } from '../api/transactions'
import { getProperty } from '../api/properties'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'
import StatusBadge from '../components/StatusBadge.jsx'
import './MyBookings.css'

// Payment components a booking may need. TOKEN and DEPOSIT share ONE pool
// (the security deposit) — a token payment simply reduces the remaining
// deposit rather than adding an extra charge on top of it. RENT is its own
// separate pool and supports partial/installment payments — pay any amount
// up to the pending balance, as many times as needed. A booking is
// CONFIRMED once the deposit pool (token + deposit) and rent are both
// fully paid.
const PAYMENT_STAGES = [
  { type: 'TOKEN', label: 'Token amount', required: false, hint: 'Optional — holds your request while the owner reviews it.' },
  { type: 'DEPOSIT', label: 'Security deposit', required: true, hint: 'Required to confirm your booking.' },
  { type: 'RENT', label: 'Rent', required: true, hint: 'Required to confirm your booking. Can be paid in installments.' }
]

const FINAL_STATUS_LABEL = {
  NOT_PAID: 'Not Paid',
  PAYMENT_FAILED: 'Payment Failed',
  PARTIALLY_PAID: 'Partially Paid',
  FULLY_PAID: 'Payment Complete'
}

export default function MyBookings() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [transactionsByBooking, setTransactionsByBooking] = useState({})
  const [propertiesById, setPropertiesById] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  // Composite key `${bookingId}:${paymentType}` for the in-flight payment button
  const [payingKey, setPayingKey] = useState(null)
  // Custom rent amount the tenant is about to pay, keyed by bookingId
  const [rentAmountDrafts, setRentAmountDrafts] = useState({})

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    setError('')
    try {
      const data = await getBookingsByTenant(user.userId)
      setBookings(data)

      const txnEntries = await Promise.all(
        data.map(async (b) => [b.bookingId, await getTransactionsByBooking(b.bookingId).catch(() => [])])
      )
      setTransactionsByBooking(Object.fromEntries(txnEntries))

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

  // paymentType drives the amount server-side (token/deposit) — the client
  // never sends or chooses an amount for those. For RENT, an optional
  // `amount` may be sent to pay a partial installment; omitted = pay the
  // full remaining balance. Retrying a FAILED payment reuses this same
  // call; the backend creates a fresh transaction + Razorpay order for the
  // same booking — no duplicate booking is ever created.
  async function handlePay(booking, paymentType, amount) {
    setActionError('')
    const key = `${booking.bookingId}:${paymentType}`
    setPayingKey(key)

    try {
      const payload = { bookingId: booking.bookingId, paymentType }
      if (paymentType === 'RENT' && amount) {
        payload.amount = amount
      }
      const checkout = await checkoutTransaction(payload)
      openRazorpay(checkout, booking, paymentType)
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Could not start the payment.'))
      setPayingKey(null)
    }
  }

  function openRazorpay(checkout, booking, paymentType) {
    if (!window.Razorpay) {
      setActionError('Payment widget failed to load. Check your connection and try again.')
      setPayingKey(null)
      return
    }

    const rzp = new window.Razorpay({
      key: checkout.razorpayKeyId,
      amount: checkout.amountInPaise,
      currency: checkout.currency,
      order_id: checkout.razorpayOrderId,
      name: 'StaySphere',
      description: `Booking #${booking.bookingId} · ${paymentType}`,
      handler: async (response) => {
        try {
          await verifyTransaction(checkout.transactionId, {
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature
          })
          // Payment Successful is shown immediately via the refreshed
          // booking's paymentStatus (FULLY_PAID / PARTIALLY_PAID) below.
        } catch (err) {
          setActionError(apiErrorMessage(err, 'Payment succeeded but verification failed. Contact support.'))
        } finally {
          setPayingKey(null)
          load()
        }
      },
      modal: {
        ondismiss: () => {
          setPayingKey(null)
          load()
        }
      },
      theme: { color: '#16213E' }
    })
    rzp.open()
  }

  // For a given booking's transaction history, find the latest ONLINE
  // attempt per payment type so the UI can show SUCCESS / FAILED / PENDING
  // per component, independently of the others. Offline transactions are
  // always SUCCESS by construction, so they never need a "latest attempt"
  // lookup for showing Pay/Retry buttons.
  function latestOnlineAttempt(transactions, type) {
    const forType = transactions.filter((t) => t.paymentType === type && t.paymentSource !== 'OFFLINE')
    if (forType.length === 0) return null
    return forType.reduce((latest, t) =>
      (!latest || new Date(t.paymentDate ?? 0) >= new Date(latest.paymentDate ?? 0)) ? t : latest
    )
  }

  function paidSoFar(transactions, type) {
    return transactions
      .filter((t) => t.paymentType === type && t.paymentStatus === 'SUCCESS')
      .reduce((sum, t) => sum + Number(t.amount), 0)
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
            const property = propertiesById[booking.propertyId]

            // PAYMENT RULE: Token is part of the security deposit, not an
            // extra charge — so the amount shown/charged for DEPOSIT is
            // whatever remains of the deposit pool after any token already
            // paid (booking.remainingDeposit), never the property's full
            // depositAmount. All figures come straight from the backend —
            // nothing here is hardcoded or recomputed on the client.
            const amountFor = (type) => {
              if (type === 'TOKEN') return booking.tokenAmount
              if (type === 'DEPOSIT') return booking.remainingDeposit
              if (type === 'RENT') return property?.rentAmount ?? booking.totalAmount
              return null
            }

            // Only show payment stages once the owner has approved the
            // booking, and while there's still something to pay.
            const showPayments = booking.bookingStatus === 'PAYMENT_PENDING'
              || (booking.bookingStatus === 'CONFIRMED' && booking.paymentStatus !== 'FULLY_PAID')

            const canCancel = ['REQUESTED', 'PAYMENT_PENDING'].includes(booking.bookingStatus)

            const rentTotal = Number(amountFor('RENT') || 0)
            const rentPaid = paidSoFar(transactions, 'RENT')
            const rentPending = Math.max(0, rentTotal - rentPaid)
            const rentDraft = rentAmountDrafts[booking.bookingId] ?? ''

            return (
              <div key={booking.bookingId} className="booking-row">
                <div>
                  <p className="booking-meta" style={{ fontWeight: 500 }}>
                    {property?.title || `Property #${booking.propertyId}`}
                  </p>
                  <p className="booking-meta">Booking #{booking.bookingId}</p>
                  <p className="booking-meta">
                    Stay: {booking.startDate}{booking.endDate ? ` → ${booking.endDate}` : ' (open-ended)'}
                  </p>

                  {/* Final payment status — always reflects backend-computed amounts */}
                  <p className="booking-meta" style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 6 }}>
                    <StatusBadge status={booking.paymentStatus} />
                    {booking.paymentStatus === 'PARTIALLY_PAID' && (
                      <span>₹{Number(booking.amountPending).toLocaleString('en-IN')} pending</span>
                    )}
                    {booking.paymentStatus === 'FULLY_PAID' && <span>Payment Successful</span>}
                  </p>

                  {showPayments && (
                    <div className="payment-stage-list" style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {PAYMENT_STAGES.map(({ type, label, required, hint }) => {
                        const latest = latestOnlineAttempt(transactions, type)
                        const status = latest?.paymentStatus
                        const amount = amountFor(type)
                        const key = `${booking.bookingId}:${type}`
                        const isPaying = payingKey === key

                        if (type === 'RENT') {
                          // Rent supports partial payment. Fully paid once
                          // rentPending hits 0 — nothing left to pay.
                          const rentFullyPaid = rentPending <= 0
                          const isFailedLast = status === 'FAILED'
                          const isPendingAttempt = status === 'PENDING'

                          return (
                            <div key={type} className="booking-meta">
                              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <span style={{ minWidth: 130 }}>
                                  {label} — ₹{rentTotal.toLocaleString('en-IN')}
                                </span>
                                {rentFullyPaid && <StatusBadge status="SUCCESS" />}
                                {!rentFullyPaid && rentPaid > 0 && <span style={{ fontSize: '0.78rem' }}>Paid ₹{rentPaid.toLocaleString('en-IN')} / Pending ₹{rentPending.toLocaleString('en-IN')}</span>}
                                {isFailedLast && !rentFullyPaid && (
                                  <span style={{ color: 'var(--rust)', fontSize: '0.78rem' }}>Payment failed.</span>
                                )}
                              </div>

                              {!rentFullyPaid && !isPendingAttempt && (
                                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 6 }}>
                                  <input
                                    type="number"
                                    min="1"
                                    max={rentPending}
                                    placeholder={`Up to ₹${rentPending.toLocaleString('en-IN')}`}
                                    value={rentDraft}
                                    onChange={(e) => setRentAmountDrafts((prev) => ({ ...prev, [booking.bookingId]: e.target.value }))}
                                    style={{ width: 160 }}
                                  />
                                  <button
                                    className={`btn btn-sm ${isFailedLast ? 'btn-danger' : 'btn-brass'}`}
                                    onClick={() => handlePay(booking, 'RENT', rentDraft ? Number(rentDraft) : undefined)}
                                    disabled={isPaying}
                                  >
                                    {isPaying
                                      ? 'Opening payment…'
                                      : isFailedLast
                                        ? '↺ Pay Again'
                                        : rentDraft
                                          ? `Pay ₹${Number(rentDraft).toLocaleString('en-IN')}`
                                          : `Pay full ₹${rentPending.toLocaleString('en-IN')}`}
                                  </button>
                                </div>
                              )}
                              {isPendingAttempt && (
                                <span style={{ fontSize: '0.78rem' }}>Payment in progress — refresh to retry.</span>
                              )}
                            </div>
                          )
                        }

                        // TOKEN / DEPOSIT — both draw from the same deposit
                        // pool. Once the pool is fully settled (by token,
                        // deposit, or a mix of the two, online or offline),
                        // the backend reports a 0 amount for whichever stage
                        // is left — hide the pay action for that stage.
                        const settled = amount == null || Number(amount) <= 0
                        const canPay = !settled && status !== 'SUCCESS' && status !== 'PENDING'
                        return (
                          <div key={type} className="booking-meta" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <span style={{ minWidth: 130 }}>
                              {label}{required ? '' : ' (optional)'}
                              {!settled && <> — ₹{Number(amount).toLocaleString('en-IN')}</>}
                              {type === 'DEPOSIT' && <> (Remaining deposit)</>}
                            </span>
                            {status && <StatusBadge status={status} />}
                            {settled && status !== 'SUCCESS' && (
                              <StatusBadge status="SUCCESS" />
                            )}
                            {status === 'FAILED' && (
                              <span style={{ color: 'var(--rust)', fontSize: '0.78rem' }}>Payment failed.</span>
                            )}
                            {!status && !settled && <span style={{ fontSize: '0.78rem', opacity: 0.7 }}>{hint}</span>}
                            {canPay && (
                              <button
                                className={`btn btn-sm ${status === 'FAILED' ? 'btn-danger' : 'btn-brass'}`}
                                onClick={() => handlePay(booking, type)}
                                disabled={isPaying}
                              >
                                {isPaying ? 'Opening payment…' : status === 'FAILED' ? '↺ Pay Again' : `Pay ${label.toLowerCase()}`}
                              </button>
                            )}
                            {status === 'PENDING' && (
                              <span style={{ fontSize: '0.78rem' }}>Payment in progress — refresh to retry.</span>
                            )}
                          </div>
                        )
                      })}
                    </div>
                  )}

                  {/* Payment history, including offline payments the owner recorded */}
                  {transactions.length > 0 && (
                    <details style={{ marginTop: 10 }}>
                      <summary style={{ cursor: 'pointer', fontSize: '0.8rem' }}>Payment history</summary>
                      <div style={{ marginTop: 6, display: 'flex', flexDirection: 'column', gap: 4 }}>
                        {transactions.map((t) => (
                          <div key={t.transactionId} className="booking-meta" style={{ fontSize: '0.78rem' }}>
                            {t.paymentType} — ₹{Number(t.amount).toLocaleString('en-IN')} — {t.paymentStatus}
                            {t.paymentSource === 'OFFLINE' ? ' (paid to owner directly)' : ''}
                            {t.paymentDate ? ` — ${new Date(t.paymentDate).toLocaleString()}` : ''}
                          </div>
                        ))}
                      </div>
                    </details>
                  )}
                </div>

                <div className="booking-actions">
                  <StatusBadge status={booking.bookingStatus} />

                  {canCancel && (
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => handleCancel(booking.bookingId)}
                    >
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