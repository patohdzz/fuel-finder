import { useState } from 'react'
import './App.css'

function App() {
  // zipCode is the current value, setZipCode is the function used to change that value
  // initially "", then if you type a zip code react updates it
  const [zipCode, setZipCode] = useState('')
  // same thing happens here, its initially set to regular
  const [fuelType, setFuelType] = useState('REGULAR')

  // function handleSubmit(event) {
  //   // when user presses seach, the form calls handleSubmit
  //   event.preventDefault()

  //   console.log('ZIP Code:', zipCode)
  //   console.log('Fuel Type:', fuelType)
  // }
  async function handleSubmit(event) {
    event.preventDefault()

    const url =
      `http://localhost:8080/api/fuel-prices?fuelType=${fuelType}&zipCode=${zipCode}`

    try {
      // browser making an HTTP request to your Spring Boot server
      const response = await fetch(url)

      // takes the HTTP response body and parses the JSON into JavaScript data that React can work with.
      const data = await response.json()

      console.log(data)
    } catch (error) {
      console.error('Error fetching fuel prices:', error)
    }
  }

  return (
    <div className="app">
      <h1>FuelFinder</h1>

      <p>Find the cheapest fuel prices near you.</p>

      <form onSubmit={handleSubmit}> 
        <div>
          <label htmlFor="zipCode">ZIP Code</label>

          <input
            id="zipCode"
            type="text"
            value={zipCode}
            onChange={(event) => setZipCode(event.target.value)} // Whenever the user changes this input, take its new value and store it in zipCode.
            placeholder="76010"
          />
        </div>

        <div>
          <label htmlFor="fuelType">Fuel Type</label>

          <select
            id="fuelType"
            value={fuelType}
            onChange={(event) => setFuelType(event.target.value)}
          >
            <option value="REGULAR">Regular</option>
            <option value="MIDGRADE">Midgrade</option>
            <option value="PREMIUM">Premium</option>
            <option value="DIESEL">Diesel</option>
          </select>
        </div>

        <button type="submit">Search</button>
      </form>
    </div>
  )
}

export default App