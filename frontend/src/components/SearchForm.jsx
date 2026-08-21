// this component does not own the state, App will give it these values which are called props
function SearchForm({
  zipCode,
  setZipCode,
  city,
  setCity,
  cities,
  fuelType,
  setFuelType,
  tankGallons,
  onTankGallonsChange,
  handleSubmit,
  loading,
  searchError
}) {
  return (
    <form className="search-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="zipCode">ZIP Code</label>

        <input
          id="zipCode"
          type="text"
          value={zipCode}
          onChange={(event) => setZipCode(event.target.value)}
          placeholder="76010"
          maxLength="5"
          inputMode="numeric"
        />
      </div>

      <div className="form-group">
        <label htmlFor="city">City</label>

        <select
          id="city"
          value={city}
          onChange={(event) => setCity(event.target.value)}
        >
          <option value="">Any city</option>
          {cities.map((cityName) => (
            <option key={cityName} value={cityName}>
              {cityName}
            </option>
          ))}
        </select>
      </div>

      <div className="form-group">
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

      <div className="form-group">
        <label htmlFor="tankGallons">Gallons to Buy</label>

        <input
          id="tankGallons"
          type="number"
          min="1"
          max="30"
          step="1"
          value={tankGallons}
          onChange={(event) => onTankGallonsChange(Number(event.target.value))}
        />
      </div>

      <button
        className="search-button"
        type="submit"
        disabled={loading}
      >
        {loading ? 'Searching...' : 'Search'}
      </button>

      {searchError && (
        <p className="form-error">{searchError}</p>
      )}
    </form>
  )
}

export default SearchForm
