import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  getProperty, getPropertyImages, addPropertyImage, setPrimaryImage, deletePropertyImage,
  getFacilities, addFacility, deleteFacility
} from '../api/properties'
import { apiErrorMessage } from '../api/client'
import './OwnerPropertyManage.css'

export default function OwnerPropertyManage() {
  const { propertyId } = useParams()
  const [property, setProperty] = useState(null)
  const [images, setImages] = useState([])
  const [facilities, setFacilities] = useState([])
  const [error, setError] = useState('')

  const [newImageUrl, setNewImageUrl] = useState('')
  const [newFacility, setNewFacility] = useState('')

  useEffect(() => {
    load()
  }, [propertyId])

  async function load() {
    try {
      const [p, imgs, facs] = await Promise.all([
        getProperty(propertyId),
        getPropertyImages(propertyId),
        getFacilities(propertyId)
      ])
      setProperty(p)
      setImages(imgs)
      setFacilities(facs)
    } catch {
      setError('Could not load this property.')
    }
  }

  async function handleAddImage(e) {
    e.preventDefault()
    if (!newImageUrl.trim()) return
    try {
      await addPropertyImage(propertyId, { imageUrl: newImageUrl.trim(), primary: images.length === 0 })
      setNewImageUrl('')
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not add that image.'))
    }
  }

  async function handleSetPrimary(imageId) {
    try {
      await setPrimaryImage(imageId)
      load()
    } catch {
      setError('Could not set that as the primary image.')
    }
  }

  async function handleDeleteImage(imageId) {
    try {
      await deletePropertyImage(imageId)
      load()
    } catch {
      setError('Could not remove that image.')
    }
  }

  async function handleAddFacility(e) {
    e.preventDefault()
    if (!newFacility.trim()) return
    try {
      await addFacility(propertyId, newFacility.trim())
      setNewFacility('')
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not add that facility.'))
    }
  }

  async function handleDeleteFacility(facilityId) {
    try {
      await deleteFacility(facilityId)
      load()
    } catch {
      setError('Could not remove that facility.')
    }
  }

  if (!property) return <div className="spinner-row">Loading…</div>

  return (
    <div className="page">
      <div className="container">
        <Link to="/owner/properties" className="back-link">← Back to my properties</Link>
        <h1>{property.title}</h1>
        {error && <div className="banner-error">{error}</div>}

        <div className="manage-grid">
          <div>
            <h2>Photos</h2>
            <form className="inline-add-form" onSubmit={handleAddImage}>
              <input
                placeholder="Image URL"
                value={newImageUrl}
                onChange={(e) => setNewImageUrl(e.target.value)}
              />
              <button className="btn btn-brass btn-sm" type="submit">Add</button>
            </form>

            <div className="image-list">
              {images.map((img) => (
                <div key={img.imageId} className="image-row">
                  <img src={img.imageUrl} alt="" onError={(e) => (e.target.style.display = 'none')} />
                  <span className="image-url">{img.imageUrl}</span>
                  {img.primary ? (
                    <span className="tag tag-active">Primary</span>
                  ) : (
                    <button className="btn btn-outline btn-sm" onClick={() => handleSetPrimary(img.imageId)}>
                      Make primary
                    </button>
                  )}
                  <button className="btn btn-danger btn-sm" onClick={() => handleDeleteImage(img.imageId)}>Remove</button>
                </div>
              ))}
              {images.length === 0 && <p className="booking-meta">No photos added yet.</p>}
            </div>
          </div>

          <div>
            <h2>Facilities</h2>
            <form className="inline-add-form" onSubmit={handleAddFacility}>
              <input
                placeholder="e.g. WiFi, Parking, Laundry"
                value={newFacility}
                onChange={(e) => setNewFacility(e.target.value)}
              />
              <button className="btn btn-brass btn-sm" type="submit">Add</button>
            </form>

            <div className="facility-list" style={{ marginTop: 12 }}>
              {facilities.map((f) => (
                <span key={f.facilityId} className="facility-chip">
                  {f.facilityName}
                  <button className="chip-remove" onClick={() => handleDeleteFacility(f.facilityId)}>×</button>
                </span>
              ))}
              {facilities.length === 0 && <p className="booking-meta">No facilities added yet.</p>}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
