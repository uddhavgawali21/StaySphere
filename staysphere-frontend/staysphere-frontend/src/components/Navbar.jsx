import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import './Navbar.css'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand">
          <span className="brand-mark">⌘</span>
          StaySphere
        </Link>

        <nav className="nav-links">
          <Link to="/">Browse</Link>
          {user?.role === 'TENANT' && <Link to="/my-bookings">My bookings</Link>}
          {user?.role === 'OWNER' && <Link to="/owner/dashboard">Dashboard</Link>}
          {user?.role === 'OWNER' && <Link to="/owner/properties">My properties</Link>}
          {user?.role === 'OWNER' && <Link to="/owner/bookings">Requests</Link>}
          {user?.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
        </nav>

        <div className="nav-auth">
          {user ? (
            <>
              <span className="nav-user">{user.firstName}</span>
              <button className="btn btn-outline btn-sm" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline btn-sm">Log in</Link>
              <Link to="/register" className="btn btn-brass btn-sm">Sign up</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
