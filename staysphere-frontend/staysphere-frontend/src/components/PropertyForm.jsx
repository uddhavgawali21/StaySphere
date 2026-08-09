import { useState } from 'react'

const blank = {
  title: '',
  description: '',
  propertyType: 'ROOM',
  rentAmount: '',
  depositAmount: '',
  occupancyType: 'SINGLE',
  totalRooms: 1,
  addressLine: '',
  area: '',
  city: '',
  state: '',
  pincode: ''
}

export default function PropertyForm({ initialValues, onSubmit, onCancel, submitLabel = 'Save' }) {
  const [form, setForm] = useState({ ...blank, ...initialValues })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      await onSubmit({
        ...form,
        rentAmount: Number(form.rentAmount),
        depositAmount: Number(form.depositAmount),
        totalRooms: Number(form.totalRooms) || 1
      })
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not save this property.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="form-card" style={{ maxWidth: 640 }} onSubmit={handleSubmit}>
      {error && <div className="banner-error">{error}</div>}

      <div className="field">
        <label>Title</label>
        <input name="title" required value={form.title} onChange={handleChange} />
      </div>
      <div className="field">
        <label>Description</label>
        <textarea name="description" value={form.description} onChange={handleChange} />
      </div>

      <div className="form-grid">
        <div className="field">
          <label>Property type</label>
          <select name="propertyType" value={form.propertyType} onChange={handleChange}>
            <option value="ROOM">Room</option>
            <option value="PG">PG</option>
            <option value="FLAT">Flat</option>
            <option value="HOSTEL">Hostel</option>
          </select>
        </div>
        <div className="field">
          <label>Occupancy</label>
          <select name="occupancyType" value={form.occupancyType} onChange={handleChange}>
            <option value="SINGLE">Single</option>
            <option value="SHARED">Shared</option>
          </select>
        </div>
        <div className="field">
          <label>Rent (₹/month)</label>
          <input type="number" name="rentAmount" required min="1" value={form.rentAmount} onChange={handleChange} />
        </div>
        <div className="field">
          <label>Deposit (₹)</label>
          <input type="number" name="depositAmount" required min="0" value={form.depositAmount} onChange={handleChange} />
        </div>
        {/* FIX: total rooms — lets PG/HOSTEL owners specify how many rooms are available */}
        <div className="field">
          <label>Total rooms</label>
          <input
            type="number"
            name="totalRooms"
            required
            min="1"
            value={form.totalRooms}
            onChange={handleChange}
            title="For a single room or flat, leave as 1. For a PG with e.g. 10 rooms, enter 10."
          />
        </div>
      </div>

      <div className="field">
        <label>Address line</label>
        <input name="addressLine" required value={form.addressLine} onChange={handleChange} />
      </div>

      <div className="form-grid">
        <div className="field">
          <label>Area</label>
          <input name="area" required value={form.area} onChange={handleChange} />
        </div>
        <div className="field">
          <label>City</label>
          <input name="city" required value={form.city} onChange={handleChange} />
        </div>
        <div className="field">
          <label>State</label>
          <input name="state" required value={form.state} onChange={handleChange} />
        </div>
        <div className="field">
          <label>Pincode</label>
          <input name="pincode" required value={form.pincode} onChange={handleChange} />
        </div>
      </div>

      <div style={{ display: 'flex', gap: 10 }}>
        <button className="btn btn-primary" type="submit" disabled={saving}>
          {saving ? 'Saving…' : submitLabel}
        </button>
        {onCancel && (
          <button className="btn btn-outline" type="button" onClick={onCancel}>Cancel</button>
        )}
      </div>
    </form>
  )
}