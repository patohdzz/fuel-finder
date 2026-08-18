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

function StationCard({
  result,
  isCheapest,
  onUpdatePrice
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

    if (price > 10) {
      setUpdateMessage('Price cannot be greater than $10')
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

  return (
    <article
      className={`station-card ${isCheapest ? 'cheapest-card' : ''}`}
    >
      <div className="station-card-header">
        <div>
          {isCheapest && (
            <span className="best-price-badge">
              Best Price
            </span>
          )}

          <h3>{result.stationName}</h3>
        </div>

        <div className="price">
          ${result.price.toFixed(2)}
        </div>
      </div>

      <p className="fuel-type">
        {result.fuelType}
      </p>

      <p className="station-address">
        {result.address}
        <br />
        {result.city}, {result.state} {result.zipCode}
      </p>

      <p className="last-updated">
        {formatTimeAgo(result.lastUpdated)}
      </p>

      <button
        className="update-price-button"
        type="button"
        onClick={() => {
          setShowUpdateForm(!showUpdateForm)
          setUpdateMessage('')
        }}
      >
        {showUpdateForm ? 'Cancel' : 'Update Price'}
      </button>

      {showUpdateForm && (
        <form
          className="update-price-form"
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
            max="10"
            value={newPrice}
            onChange={(event) => setNewPrice(event.target.value)}
            placeholder={result.price.toFixed(2)}
          />

          <button type="submit" disabled={updating}>
            {updating ? 'Updating...' : 'Submit'}
          </button>
        </form>
      )}

      {updateMessage && (
        <p className="update-message">
          {updateMessage}
        </p>
      )}
    </article>
  )
}

export default StationCard