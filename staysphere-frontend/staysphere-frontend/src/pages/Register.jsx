import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'

const initialForm = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
  role: 'TENANT'
}

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      setSuccess(true)
      setTimeout(() => navigate('/login'), 1200)
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not create your account.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="container" style={{ display: 'flex', justifyContent: 'center' }}>
        <form className="form-card" style={{ maxWidth: 520 }} onSubmit={handleSubmit}>
          <h2>Create your account</h2>
          <p style={{ marginBottom: 24 }}>Whether you're moving in or renting out — start here.</p>

          {error && <div className="banner-error">{error}</div>}
          {success && (
            <div className="banner-error" style={{ background: 'var(--sage-dim)', color: 'var(--sage)' }}>
              Account created — redirecting to log in…
            </div>
          )}

          <div className="form-grid">
            <div className="field">
              <label>First name</label>
              <input name="firstName" required value={form.firstName} onChange={handleChange} />
            </div>
            <div className="field">
              <label>Last name</label>
              <input name="lastName" required value={form.lastName} onChange={handleChange} />
            </div>
          </div>

          <div className="field">
            <label>Email</label>
            <input type="email" name="email" required value={form.email} onChange={handleChange} />
          </div>
          <div className="field">
            <label>Phone</label>
            <input name="phone" required value={form.phone} onChange={handleChange} />
          </div>
          <div className="field">
            <label>Password</label>
            <input type="password" name="password" required value={form.password} onChange={handleChange} />
          </div>

          <div className="field">
            <label>I want to</label>
            <select name="role" value={form.role} onChange={handleChange}>
              <option value="TENANT">Find a room to rent</option>
              <option value="OWNER">List my property</option>
            </select>
          </div>

          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>

          <p style={{ marginTop: 18, fontSize: '0.88rem' }}>
            Already have an account? <Link to="/login">Log in</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
