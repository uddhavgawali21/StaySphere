import { useEffect, useState } from 'react'
import { getMyPaymentAccount, saveMyPaymentAccount } from '../api/ownerPaymentAccount'
import { apiErrorMessage } from '../api/client'
import './OwnerPaymentAccount.css'

const emptyForm = {
  accountHolderName: '',
  upiId: '',
  bankAccountNumber: '',
  ifscCode: '',
  bankName: ''
}

export default function OwnerPaymentAccount() {
  const [form, setForm] = useState(emptyForm)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [hasAccount, setHasAccount] = useState(false)

  useEffect(() => {
    async function load() {
      try {
        const data = await getMyPaymentAccount()
        setForm({
          accountHolderName: data.accountHolderName || '',
          upiId: data.upiId || '',
          bankAccountNumber: data.bankAccountNumber || '',
          ifscCode: data.ifscCode || '',
          bankName: data.bankName || ''
        })
        setHasAccount(true)
      } catch {
        // No account configured yet — start from an empty form.
        setHasAccount(false)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)
    try {
      const payload = {
        accountHolderName: form.accountHolderName,
        upiId: form.upiId || null,
        bankAccountNumber: form.bankAccountNumber || null,
        ifscCode: form.ifscCode ? form.ifscCode.toUpperCase() : null,
        bankName: form.bankName || null
      }
      await saveMyPaymentAccount(payload)
      setHasAccount(true)
      setSuccess('Payout account saved. Tenant payments for your properties will go here.')
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not save your payout account.'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="spinner-row">Loading…</div>

  return (
    <div className="page">
      <div className="container narrow">
        <h1>Payout account</h1>
        <p className="page-subtitle">
          Tenant payments for your properties are sent to this account. Add a UPI id, or a bank
          account number with IFSC code, before approving a booking — payment cannot proceed
          without it.
        </p>

        {!hasAccount && (
          <div className="banner-error" style={{ background: 'var(--brass-dim)', color: 'var(--brass)' }}>
            You haven't configured a payout account yet. Tenants cannot pay you until you add one.
          </div>
        )}
        {error && <div className="banner-error">{error}</div>}
        {success && (
          <div className="banner-error" style={{ background: 'var(--sage-dim)', color: 'var(--sage)' }}>
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="payment-account-form">
          <div className="field">
            <label>Account holder name</label>
            <input
              name="accountHolderName"
              value={form.accountHolderName}
              onChange={handleChange}
              required
              maxLength={150}
            />
          </div>

          <div className="field">
            <label>UPI id</label>
            <input
              name="upiId"
              value={form.upiId}
              onChange={handleChange}
              placeholder="yourname@bank"
            />
          </div>

          <p className="form-divider">— or —</p>

          <div className="field">
            <label>Bank account number</label>
            <input
              name="bankAccountNumber"
              value={form.bankAccountNumber}
              onChange={handleChange}
              placeholder="1234567890123"
            />
          </div>

          <div className="field">
            <label>IFSC code</label>
            <input
              name="ifscCode"
              value={form.ifscCode}
              onChange={handleChange}
              placeholder="HDFC0001234"
              style={{ textTransform: 'uppercase' }}
            />
          </div>

          <div className="field">
            <label>Bank name (optional)</label>
            <input name="bankName" value={form.bankName} onChange={handleChange} />
          </div>

          <button className="btn btn-primary" type="submit" disabled={saving}>
            {saving ? 'Saving…' : 'Save payout account'}
          </button>
        </form>
      </div>
    </div>
  )
}