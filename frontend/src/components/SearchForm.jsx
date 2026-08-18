// this component does not own the state, App will give it these values which are called props
function SearchForm({
  zipCode,
  setZipCode,
  fuelType,
  setFuelType,
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
        {searchError && (
          <p className="form-error">{searchError}</p>
        )}
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

      <button
        className="search-button"
        type="submit"
        disabled={loading}
      >
        {loading ? 'Searching...' : 'Search'}
      </button>
    </form>
  )
}

export default SearchForm