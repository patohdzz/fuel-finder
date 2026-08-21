import { useState } from 'react'
import StationCard from './StationCard'
import StationsMap from './StationsMap'
import { milesBetween, estimateRoundTripCost, estimateValuePricePerGallon } from '../utils/distance'

function SearchResults({
  results,
  loading,
  error,
  hasSearched,
  onUpdatePrice,
  userLocation,
  tankGallons
}) {
  const [selectedStationId, setSelectedStationId] = useState(null)

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
        No gas stations found for this search.
      </div>
    )
  }

  const firstPricedStation = results.find((result) => result.price !== null)
  const cheapestPrice = firstPricedStation ? firstPricedStation.price : null

  const cheapestStationIds = new Set(
    results
      .filter((result) => cheapestPrice !== null && result.price === cheapestPrice)
      .map((result) => result.stationId)
  )

  // Only computed once the user opts in via LocationOptIn and sets how many
  // gallons they're buying. "Total cost" = pump price, plus the estimated
  // round-trip drive cost spread across those gallons as a $/gallon
  // surcharge -- keeps the ranking in real price-per-gallon terms instead
  // of adding a lump-sum drive cost onto a per-gallon price.
  const bestValueResults = userLocation && tankGallons > 0
    ? results
        .filter(
          (result) =>
            result.latitude != null &&
            result.longitude != null &&
            result.price !== null
        )
        .map((result) => {
          const distanceMiles = milesBetween(
            userLocation.latitude,
            userLocation.longitude,
            result.latitude,
            result.longitude
          )
          const roundTripCost = estimateRoundTripCost(distanceMiles, result.price)
          const driveCostPerGallon = roundTripCost / tankGallons
          const totalCost = estimateValuePricePerGallon(result.price, roundTripCost, tankGallons)

          return {
            ...result,
            distanceMiles,
            roundTripCost,
            driveCostPerGallon,
            totalCost
          }
        })
        .sort((a, b) => a.totalCost - b.totalCost)
    : []

  const bestValuePrice = bestValueResults.length > 0 ? bestValueResults[0].totalCost : null
  const hasBestValue = userLocation && bestValueResults.length > 0

  const bestValueStationIds = new Set(
    bestValueResults
      .filter((result) => bestValuePrice !== null && result.totalCost === bestValuePrice)
      .map((result) => result.stationId)
  )

  return (
    <section className="results-section has-map">
      <div className={`results-columns ${hasBestValue ? 'results-columns--three' : ''}`}>
        <StationsMap
          results={results}
          userLocation={userLocation}
          selectedStationId={selectedStationId}
          cheapestStationIds={cheapestStationIds}
          bestValueStationIds={bestValueStationIds}
        />

        <div className="results-column">
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
                isCheapest={cheapestPrice !== null && result.price === cheapestPrice}
                showDriveCost={false}
                onUpdatePrice={onUpdatePrice}
                onSelect={() => setSelectedStationId(result.stationId)}
              />
            ))}
          </div>
        </div>

        {hasBestValue && (
          <div className="results-column">
            <div className="results-header">
              <h2>Best Value</h2>
              <p>Price per gallon including the drive there, based on a {tankGallons}-gallon fill-up</p>
            </div>

            <div className="results-list">
              {bestValueResults.map((result) => (
                <StationCard
                  key={result.stationId}
                  result={result}
                  isCheapest={result.totalCost === bestValuePrice}
                  showDriveCost
                  onUpdatePrice={onUpdatePrice}
                  onSelect={() => setSelectedStationId(result.stationId)}
                />
              ))}
            </div>
          </div>
        )}
      </div>
    </section>
  )
}

export default SearchResults
