import StationCard from './StationCard'

function SearchResults({
  results,
  loading,
  error,
  hasSearched,
  onUpdatePrice
}) {
  if (!hasSearched) {
    return null
  }

  if (loading) {
    return (
      <div className="status-message">
        Searching for fuel prices...
      </div>
    )
  }

  if (error) {
    return (
      <div className="status-message error-message">
        {error}
      </div>
    )
  }

  if (results.length === 0) {
    return (
      <div className="status-message">
        No fuel prices found for this search.
      </div>
    )
  }

  const cheapestPrice = results[0].price

  return (
    <section className="results-section">
      <div className="results-header">
        <h2>Fuel Prices</h2>

        <p>
          {results.length} {results.length === 1 ? 'station' : 'stations'} found
        </p>
      </div>

      <div className="results-list">
        {results.map((result) => (
          <StationCard
            key={result.stationId}
            result={result}
            isCheapest={result.price === cheapestPrice}
            onUpdatePrice={onUpdatePrice}
          />
        ))}
      </div>
    </section>
  )
}

export default SearchResults