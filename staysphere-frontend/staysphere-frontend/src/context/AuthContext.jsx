import { createContext, useContext, useEffect, useState } from 'react'
import { loginUser, registerUser } from '../api/auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [initializing, setInitializing] = useState(true)

  useEffect(() => {
    const storedUser = localStorage.getItem('staysphere_user')
    const storedToken = localStorage.getItem('staysphere_token')
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser))
    }
    setInitializing(false)
  }, [])

  async function login(credentials) {
    const response = await loginUser(credentials)
    localStorage.setItem('staysphere_token', response.token)
    localStorage.setItem('staysphere_user', JSON.stringify(response.user))
    setUser(response.user)
    return response.user
  }

  async function register(payload) {
    return registerUser(payload)
  }

  function logout() {
    localStorage.removeItem('staysphere_token')
    localStorage.removeItem('staysphere_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, initializing, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
