import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'

import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import PropertyDetails from './pages/PropertyDetails.jsx'
import MyBookings from './pages/MyBookings.jsx'
import OwnerDashboard from './pages/OwnerDashboard.jsx'
import OwnerProperties from './pages/OwnerProperties.jsx'
import OwnerPropertyManage from './pages/OwnerPropertyManage.jsx'
import OwnerBookings from './pages/OwnerBookings.jsx'
import OwnerPaymentAccount from './pages/OwnerPaymentAccount.jsx'
import AdminPanel from './pages/AdminPanel.jsx'

export default function App() {
  return (
    <div className="app-shell">
      <Navbar />

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/properties/:propertyId" element={<PropertyDetails />} />

        <Route
          path="/my-bookings"
          element={
            <ProtectedRoute roles={['TENANT']}>
              <MyBookings />
            </ProtectedRoute>
          }
        />

        <Route
          path="/owner/dashboard"
          element={
            <ProtectedRoute roles={['OWNER']}>
              <OwnerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/properties"
          element={
            <ProtectedRoute roles={['OWNER']}>
              <OwnerProperties />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/properties/:propertyId/manage"
          element={
            <ProtectedRoute roles={['OWNER']}>
              <OwnerPropertyManage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/bookings"
          element={
            <ProtectedRoute roles={['OWNER']}>
              <OwnerBookings />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/payment-account"
          element={
            <ProtectedRoute roles={['OWNER']}>
              <OwnerPaymentAccount />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminPanel />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Home />} />
      </Routes>
    </div>
  )
}