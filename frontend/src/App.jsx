import { useState } from 'react'
import './App.css'
import SearchForm from './components/SearchForm'
import SearchResults from './components/SearchResults'

function App() {
  // zipCode is the current value, setZipCode is the function used to change that value
  // initially "", then if you type a zip code react updates it
  const [zipCode, setZipCode] = useState('')
  // same thing happens here, its initially set to regular
  const [fuelType, setFuelType] = useState('REGULAR')
  const [results, setResults] = useState([])            // What did Spring Boot return?
  const [loading, setLoading] = useState(false)         // Is the API request still running?
  const [error, setError] = useState('')                // Did something go wrong?
  const [hasSearched, setHasSearched] = useState(false) // Has the user actually searched yet?

  async function searchFuelPrices() {
    // Deals with searching the backend
    const url = `http://localhost:8080/api/fuel-prices?fuelType=${fuelType}&zipCode=${zipCode}`

    setLoading(true)
    setError('')
    setHasSearched(true)

    try {
      const response = await fetch(url)

      if (!response.ok) {
        throw new Error('Request failed')
      }

      const data = await response.json()
      setResults(data)
      // setState(...)
      //     ↓
      // state changes
      //     ↓
      // React re-renders
      //     ↓
      // UI changes
    } catch (error) {
      console.error('Error fetching fuel prices:', error)

      setResults([])
      setError('Unable to load fuel prices. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  // Now other functions can also call searchFuelPrices
  function handleSubmit(event) {
    // Deals with the form
    event.preventDefault()

    searchFuelPrices()
  }

  // The browser will send JSON like:
  // {
  //   "price": 2.75,
  //   "fuelType": "REGULAR"
  // }
  // to: POST /api/stations/1/prices
  async function handlePriceUpdate(stationId, fuelType, newPrice) {
    const url = `http://localhost:8080/api/stations/${stationId}/prices`

    const response = await fetch(url, { // Don't continue until the update request has finished.
      method: 'POST',

      headers: {
        'Content-Type': 'application/json'
      },

      body: JSON.stringify({ // converts it into JSON suitable for the HTTP request body.
        price: newPrice,
        fuelType: fuelType
      })
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message || 'Unable to update fuel price.')
    }

    // this tells React to run the search again
    await searchFuelPrices()

    return data
  }

  return (
    <main className="app">
      {/* search area */}
      <section className="hero"> 
        <h1>FuelFinder</h1>

        <p>
          Find the cheapest fuel prices near you.
        </p>

        <SearchForm
          zipCode={zipCode}
          setZipCode={setZipCode}
          fuelType={fuelType}
          setFuelType={setFuelType}
          handleSubmit={handleSubmit}
          loading={loading}
        />
      </section>

      {/* search results */}
      <SearchResults
        results={results}
        loading={loading}
        error={error}
        hasSearched={hasSearched}
        onUpdatePrice={handlePriceUpdate}
      />
    </main>
  )
}

export default App