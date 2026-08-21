import { useState, useEffect } from 'react'
import './App.css'
import SearchForm from './components/SearchForm'
import SearchResults from './components/SearchResults'
import useUserLocation from './hooks/useUserLocation'
import LocationOptIn from './components/LocationOptIn'

const API_URL = import.meta.env.VITE_API_URL

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
  const [searchError, setSearchError] = useState('')

  const { location: userLocation, requestLocation, isLoading: locationLoading } = useUserLocation()
  const [tankGallons, setTankGallons] = useState(10) // how many gallons the user plans to buy, for the Best Value calculation
  const [city, setCity] = useState('')
  const [cities, setCities] = useState([]) // real cities that have stations, for the dropdown

  useEffect(() => {
    fetch(`${API_URL}/api/stations/cities`)
      .then((response) => response.json())
      .then(setCities)
      .catch(() => setCities([]))
  }, [])


  async function searchFuelPrices() {
    // Deals with searching the backend
    const params = new URLSearchParams({fuelType})
    if (zipCode) params.set('zipCode', zipCode)
    if (city.trim()) params.set('city', city.trim())
    const url = `${API_URL}/api/stations/search?${params.toString()}`

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
    event.preventDefault()

    const trimmedCity = city.trim()
    const hasZip = zipCode.length > 0
    const hasCity = trimmedCity.length > 0

    if (!hasZip && !hasCity) {
      setSearchError('Enter a ZIP code, a city, or both.')
      return
    }

    const zipPattern = /^\d{5}$/
    if (hasZip && !zipPattern.test(zipCode)) {
      setSearchError('ZIP code must contain exactly 5 digits.')
      return
    }

    setSearchError('')
    searchFuelPrices()
  }


  // The browser will send JSON like:
  // {
  //   "price": 2.75,
  //   "fuelType": "REGULAR"
  // }
  // to: POST /api/stations/1/prices
  async function handlePriceUpdate(stationId, fuelType, newPrice) {
    const url = `${API_URL}/api/stations/${stationId}/prices`

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

        <p className="hero-instructions">
          Enter a ZIP code, a city, or both — plus your fuel type — to compare local prices.
        </p>

        <SearchForm
          zipCode={zipCode}
          setZipCode={setZipCode}
          city={city}
          setCity={setCity}
          cities={cities}
          fuelType={fuelType}
          setFuelType={setFuelType}
          tankGallons={tankGallons}
          onTankGallonsChange={setTankGallons}
          handleSubmit={handleSubmit}
          loading={loading}
          searchError={searchError}
        />

        <LocationOptIn
          userLocation={userLocation}
          onRequestLocation={requestLocation}
          isLoading={locationLoading}
        />

      </section>

      {/* search results */}
      <SearchResults
        results={results}
        loading={loading}
        error={error}
        hasSearched={hasSearched}
        onUpdatePrice={handlePriceUpdate}
        userLocation={userLocation}
        tankGallons={tankGallons}

      />

      <footer className="app-footer">
        <a href="https://github.com/patohdzz/fuel-finder" target="_blank" rel="noreferrer">
          GitHub
        </a>
        <span className="footer-divider">·</span>
        Station data &copy;{' '}
        <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">
          OpenStreetMap
        </a>{' '}
        contributors
      </footer>
    </main>
  )

}

export default App