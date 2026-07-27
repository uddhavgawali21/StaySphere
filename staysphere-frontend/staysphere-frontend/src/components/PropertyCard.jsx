import { Link } from 'react-router-dom'
import './PropertyCard.css'

export default function PropertyCard({ property }) {
  return (
    <Link to={`/properties/${property.propertyId}`} className="property-card">
      <div className="property-arch">
        <span className="property-arch-type">{property.propertyType}</span>
        <span className="property-tag">
          ₹{Number(property.rentAmount).toLocaleString('en-IN')}
          <small>/mo</small>
        </span>
      </div>
      <div className="property-body">
        <h3>{property.title}</h3>
        <p className="property-location">{property.area}, {property.city}</p>
        <div className="property-meta">
          <span>{property.occupancyType}</span>
          <span className="dot">·</span>
          <span>Deposit ₹{Number(property.depositAmount).toLocaleString('en-IN')}</span>
        </div>
      </div>
    </Link>
  )
}
