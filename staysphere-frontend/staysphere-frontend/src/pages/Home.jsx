import { useEffect, useMemo, useState } from 'react'
import { searchProperties } from '../api/properties'
import PropertyCard from '../components/PropertyCard.jsx'
import './Home.css'

const emptyFilters = { city: '', propertyType: '', occupancyType: '', minRent: '', maxRent: '' }
const PAGE_SIZE = 9

export default function Home() {
  const [allProperties, setAllProperties] = useState([])
  const [filters, setFilters] = useState(emptyFilters)
  const [appliedFilters, setAppliedFilters] = useState(emptyFilters)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    load()
  }, [])

  async function load() {
    setLoading(true)
    setError('')
    try {
      // Backend currently returns every ACTIVE property with no server-side
      // filtering — fetch once, then filter/paginate below on the client.
      const data = await searchProperties()
setAllProperties(data.content || [])
    } catch {
      setError('Could not load listings right now.')
    } finally {
      setLoading(false)
    }
  }

  function handleFilterChange(e) {
    setFilters({ ...filters, [e.target.name]: e.target.value })
  }

  function handleSearchSubmit(e) {
    e.preventDefault()
    setAppliedFilters(filters)
    setPage(0)
  }

  const filtered = useMemo(() => {
    return allProperties.filter((p) => {
      if (appliedFilters.city && !p.city?.toLowerCase().includes(appliedFilters.city.toLowerCase())) return false
      if (appliedFilters.propertyType && p.propertyType !== appliedFilters.propertyType) return false
      if (appliedFilters.occupancyType && p.occupancyType !== appliedFilters.occupancyType) return false
      if (appliedFilters.minRent && Number(p.rentAmount) < Number(appliedFilters.minRent)) return false
      if (appliedFilters.maxRent && Number(p.rentAmount) > Number(appliedFilters.maxRent)) return false
      return true
    })
  }, [allProperties, appliedFilters])

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const pageItems = filtered.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE)

  return (
    <div className="page">
      <section className="hero">
        <div className="container hero-inner">
          <span className="hero-eyebrow">Find your next door</span>
          <h1>A room worth <em>coming home to.</em></h1>
          <p className="hero-sub">
            Search verified rooms, PGs and flats — filter by city, budget, and how you want to live.
          </p>

          <form className="search-bar" onSubmit={handleSearchSubmit}>
            <input
              name="city"
              placeholder="City — e.g. Pune"
              value={filters.city}
              onChange={handleFilterChange}
            />
            <select name="propertyType" value={filters.propertyType} onChange={handleFilterChange}>
              <option value="">Any type</option>
              <option value="ROOM">Room</option>
              <option value="PG">PG</option>
              <option value="FLAT">Flat</option>
              <option value="HOSTEL">Hostel</option>
            </select>
            <select name="occupancyType" value={filters.occupancyType} onChange={handleFilterChange}>
              <option value="">Any occupancy</option>
              <option value="SINGLE">Single</option>
              <option value="SHARED">Shared</option>
            </select>
            <input
              name="minRent"
              type="number"
              placeholder="Min ₹"
              value={filters.minRent}
              onChange={handleFilterChange}
            />
            <input
              name="maxRent"
              type="number"
              placeholder="Max ₹"
              value={filters.maxRent}
              onChange={handleFilterChange}
            />
            <button className="btn btn-brass" type="submit">Search</button>
          </form>
        </div>
      </section>

      <div className="container">
        {loading && <div className="spinner-row">Loading listings…</div>}
        {error && <div className="banner-error">{error}</div>}

        {!loading && !error && filtered.length === 0 && (
          <div className="empty-state">
            <h3>No rooms match those filters</h3>
            <p>Try widening your budget or clearing a filter.</p>
          </div>
        )}

        {!loading && filtered.length > 0 && (
          <>
            <div className="results-grid">
              {pageItems.map((property) => (
                <PropertyCard key={property.propertyId} property={property} />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="pagination">
                <button
                  className="btn btn-outline btn-sm"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  ← Prev
                </button>
                <span>Page {page + 1} of {totalPages}</span>
                <button
                  className="btn btn-outline btn-sm"
                  disabled={page + 1 >= totalPages}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next →
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}