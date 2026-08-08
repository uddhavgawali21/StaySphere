import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import './Navbar.css'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  function closeMenu() {
    setMenuOpen(false)
  }

  function handleLogout() {
    closeMenu()
    logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand" onClick={closeMenu}>
          <span className="brand-mark">⌘</span>
          StaySphere
        </Link>

        <button
          type="button"
          className="nav-toggle"
          aria-label="Toggle menu"
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span />
          <span />
          <span />
        </button>

        <div className={`nav-menu ${menuOpen ? 'nav-menu-open' : ''}`}>
          <nav className="nav-links">
            <Link to="/" onClick={closeMenu}>Browse</Link>
            {user?.role === 'TENANT' && <Link to="/my-bookings" onClick={closeMenu}>My bookings</Link>}
            {user?.role === 'OWNER' && <Link to="/owner/dashboard" onClick={closeMenu}>Dashboard</Link>}
            {user?.role === 'OWNER' && <Link to="/owner/properties" onClick={closeMenu}>My properties</Link>}
            {user?.role === 'OWNER' && <Link to="/owner/bookings" onClick={closeMenu}>Requests</Link>}
            {user?.role === 'ADMIN' && <Link to="/admin" onClick={closeMenu}>Admin</Link>}
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
                <Link to="/login" className="btn btn-outline btn-sm" onClick={closeMenu}>Log in</Link>
                <Link to="/register" className="btn btn-brass btn-sm" onClick={closeMenu}>Sign up</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}