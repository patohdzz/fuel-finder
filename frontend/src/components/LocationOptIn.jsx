function LocationPinIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M12 21s-7-6.4-7-11a7 7 0 0 1 14 0c0 4.6-7 11-7 11z" />
      <circle cx="12" cy="10" r="2.5" />
    </svg>
  )
}

function LocationOptIn({
  userLocation,
  onRequestLocation,
  isLoading
}) {
  return (
    <div className="location-opt-in">
      {userLocation ? (
        <p className="location-status">
          <LocationPinIcon />
          Using your location to show Best Value pricing
        </p>
      ) : (
        <button
          type="button"
          className="location-button"
          onClick={onRequestLocation}
          disabled={isLoading}
        >
          <LocationPinIcon />
          Use my location for Best Value pricing
          {isLoading && <span className="spinner" aria-hidden="true" />}
        </button>
      )}
    </div>
  )
}

export default LocationOptIn
