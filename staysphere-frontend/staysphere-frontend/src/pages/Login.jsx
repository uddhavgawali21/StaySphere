import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(form)
      navigate('/')
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not log in. Check your email and password.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="container" style={{ display: 'flex', justifyContent: 'center' }}>
        <form className="form-card" onSubmit={handleSubmit}>
          <h2>Welcome back</h2>
          <p style={{ marginBottom: 24 }}>Log in to book a room or manage your listings.</p>

          {error && <div className="banner-error">{error}</div>}

          <div className="field">
            <label>Email</label>
            <input type="email" name="email" required value={form.email} onChange={handleChange} />
          </div>
          <div className="field">
            <label>Password</label>
            <input type="password" name="password" required value={form.password} onChange={handleChange} />
          </div>

          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Logging in…' : 'Log in'}
          </button>

          <p style={{ marginTop: 18, fontSize: '0.88rem' }}>
            New here? <Link to="/register">Create an account</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
