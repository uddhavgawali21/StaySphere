import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPropertiesByOwner, createProperty, updateProperty, deleteProperty } from '../api/properties'
import { useAuth } from '../context/AuthContext.jsx'
import PropertyForm from '../components/PropertyForm.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import './OwnerProperties.css'

export default function OwnerProperties() {
  const { user } = useAuth()
  const [properties, setProperties] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [mode, setMode] = useState(null) // null | 'create' | propertyId being edited

  useEffect(() => {
    load()
  }, [])

  async function load() {
    setLoading(true)
    try {
      const data = await getPropertiesByOwner(user.userId)
      setProperties(data)
    } catch {
      setError('Could not load your properties.')
    } finally {
      setLoading(false)
    }
  }

  async function handleCreate(payload) {
    await createProperty(payload)
    setMode(null)
    load()
  }

  async function handleUpdate(propertyId, payload) {
    await updateProperty(propertyId, payload)
    setMode(null)
    load()
  }

  async function handleDelete(propertyId) {
    if (!window.confirm('Delete this property? This cannot be undone.')) return
    try {
      await deleteProperty(propertyId)
      load()
    } catch {
      setError('Could not delete this property.')
    }
  }

  const editingProperty = properties.find((p) => p.propertyId === mode)

  return (
    <div className="page">
      <div className="container">
        <div className="section-heading">
          <h1>My properties</h1>
          {mode === null && (
            <button className="btn btn-brass" onClick={() => setMode('create')}>+ List a property</button>
          )}
        </div>

        {error && <div className="banner-error">{error}</div>}

        {mode === 'create' && (
          <PropertyForm submitLabel="Create property" onSubmit={handleCreate} onCancel={() => setMode(null)} />
        )}

        {editingProperty && (
          <PropertyForm
            initialValues={editingProperty}
            submitLabel="Save changes"
            onSubmit={(payload) => handleUpdate(editingProperty.propertyId, payload)}
            onCancel={() => setMode(null)}
          />
        )}

        {mode === null && (
          <>
            {loading && <div className="spinner-row">Loading…</div>}

            {!loading && properties.length === 0 && (
              <div className="empty-state">
                <h3>No properties yet</h3>
                <p>List your first room, PG or flat to start receiving bookings.</p>
              </div>
            )}

            <div className="owner-property-list">
              {properties.map((p) => (
                <div key={p.propertyId} className="owner-property-row">
                  <div>
                    <h3>{p.title}</h3>
                    <p className="booking-meta">{p.area}, {p.city} · ₹{Number(p.rentAmount).toLocaleString('en-IN')}/mo</p>
                  </div>
                  <div className="owner-property-actions">
                    <StatusBadge status={p.propertyStatus} />
                    <Link className="btn btn-outline btn-sm" to={`/owner/properties/${p.propertyId}/manage`}>
                      Photos &amp; facilities
                    </Link>
                    <button className="btn btn-outline btn-sm" onClick={() => setMode(p.propertyId)}>Edit</button>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(p.propertyId)}>Delete</button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  )
}
