// given one station result, all this does it display it
// We're going to give each card some local state.

import { useState } from 'react'

function formatTimeAgo(lastUpdated) {
  const updatedTime = new Date(lastUpdated)
  const currentTime = new Date()

  const differenceInMilliseconds = currentTime - updatedTime
  const differenceInMinutes = Math.floor(
    differenceInMilliseconds / 60000
  )

  if (differenceInMinutes < 1) {
    return 'Updated just now'
  }

  if (differenceInMinutes < 60) {
    return `Updated ${differenceInMinutes} ${
      differenceInMinutes === 1 ? 'minute' : 'minutes'
    } ago`
  }

  const differenceInHours = Math.floor(
    differenceInMinutes / 60
  )

  if (differenceInHours < 24) {
    return `Updated ${differenceInHours} ${
      differenceInHours === 1 ? 'hour' : 'hours'
    } ago`
  }

  const differenceInDays = Math.floor(
    differenceInHours / 24
  )

  return `Updated ${differenceInDays} ${
    differenceInDays === 1 ? 'day' : 'days'
  } ago`
}

// Gas prices move fast -- a price reported a day ago is a real gamble.
// Flag it visually instead of letting old data look just as trustworthy
// as a fresh report.
function getStalenessClass(lastUpdated) {
  const hoursAgo = (new Date() - new Date(lastUpdated)) / (1000 * 60 * 60)

  if (hoursAgo >= 24) {
    return 'last-updated--stale-danger'
  }

  if (hoursAgo >= 12) {
    return 'last-updated--stale-warning'
  }

  return ''
}

function StationCard({
  result,
  isCheapest,
  showDriveCost,
  onUpdatePrice,
  onSelect
}) {
  const [showUpdateForm, setShowUpdateForm] = useState(false)
  const [newPrice, setNewPrice] = useState('') // This state belongs specifically to that StationCard.
  const [updating, setUpdating] = useState(false)
  const [updateMessage, setUpdateMessage] = useState('')

  async function handlePriceSubmit(event) {
    event.preventDefault()

    const price = Number(newPrice)

    if (Number.isNaN(price) || price <= 0) {
      setUpdateMessage('Please enter a valid price.')
      return
    }

    if (result.price === null && price > 10) {
      setUpdateMessage("Price cannot be greater than $10 for a station's first reported price.")
      return
    }

    if (result.price !== null && Math.abs(price - result.price) > 0.5) {
      setUpdateMessage(
        `That's too different from the current price ($${result.price.toFixed(2)}). Please double check.`
      )
      return
    }

    setUpdating(true)
    setUpdateMessage('')

    try {
      await onUpdatePrice(
        result.stationId,
        result.fuelType,
        price
      )

      setUpdateMessage('Price updated successfully.')
      setNewPrice('')
      setShowUpdateForm(false)
    } catch (error) {
      setUpdateMessage(error.message)
    } finally {
      setUpdating(false)
    }
  }

  // In the Best Value column, the headline number is price-per-gallon
  // plus the round-trip drive cost spread across the gallons being
  // bought, not just the raw pump price -- that's the whole point of
  // that column.
  const displayedPrice =
    showDriveCost && result.totalCost != null ? result.totalCost : result.price

  return (
    <article
      className={`station-card ${
        isCheapest ? (showDriveCost ? 'best-value-card' : 'cheapest-card') : ''
      }`}
      onClick={onSelect}
    >
      <div className="station-card-header">
        <div>
          {isCheapest && (
            <span className={`best-price-badge ${showDriveCost ? 'best-value-badge' : ''}`}>
              {showDriveCost ? 'Best Value' : 'Best Price'}
            </span>
          )}

          <h3>{result.stationName}</h3>
        </div>

        <div className={result.price === null ? 'no-price' : `price ${showDriveCost ? 'price--value' : ''}`}>
          {result.price === null ? 'No price reported' : `$${displayedPrice.toFixed(2)}`}
        </div>
      </div>

      <p className="fuel-type">
        {result.fuelType}
      </p>

      <div className="station-info-row">
        <p className="station-address-line">{result.address}</p>

        {!showDriveCost && result.lastUpdated && (
          <p className={`last-updated ${getStalenessClass(result.lastUpdated)}`}>
            {formatTimeAgo(result.lastUpdated)}
          </p>
        )}
      </div>

      <div className="station-info-row">
        <p className="station-address-line">
          {result.city}, {result.state} {result.zipCode}
        </p>

        {/* Updating a price from the Best Value card doesn't make sense --
            it's the same station shown on the Fuel Prices card, just re-ranked. */}
        {!showDriveCost && (
          <button
            className="update-price-button"
            type="button"
            onClick={(event) => {
              event.stopPropagation()
              setShowUpdateForm(!showUpdateForm)
              setUpdateMessage('')
            }}
          >
            {showUpdateForm ? 'Cancel' : result.price === null ? 'Report Price' : 'Update Price'}
          </button>
        )}
      </div>

      {showDriveCost && result.distanceMiles !== null && (
        <p className="drive-estimate">
          ≈{result.distanceMiles.toFixed(1)} mi away
          {result.driveCostPerGallon !== null && (
            <> · ${result.price.toFixed(2)} pump price + ${result.driveCostPerGallon.toFixed(2)}/gal for the drive</>
          )}
        </p>
      )}

      {!showDriveCost && showUpdateForm && (
        <form
          className="update-price-form"
          onClick={(event) => event.stopPropagation()}
          onSubmit={handlePriceSubmit}
        >
          <label htmlFor={`price-${result.stationId}`}>
            New Price
          </label>

          <input
            id={`price-${result.stationId}`}
            type="number"
            step="0.01"
            min="0.01"
            max={result.price === null ? '10' : undefined}
            value={newPrice}
            onChange={(event) => setNewPrice(event.target.value)}
            placeholder={result.price === null ? 'Enter price' : result.price.toFixed(2)}
          />

          <button type="submit" disabled={updating}>
            {updating ? 'Updating...' : 'Submit'}
          </button>
        </form>
      )}

      {!showDriveCost && updateMessage && (
        <p className="update-message">
          {updateMessage}
        </p>
      )}
    </article>
  )
}

export default StationCard