import { useState } from 'react'

// Exposes a function to request the browser's geolocation on demand,
// tied to an explicit opt-in button rather than firing automatically.
// Fails silently if denied or unavailable -- optional enhancement only.
function useUserLocation() {
  const [location, setLocation] = useState(null)
  const [isLoading, setIsLoading] = useState(false)

  function requestLocation() {
    if (!navigator.geolocation) {
      return
    }

    setIsLoading(true)

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        })
        setIsLoading(false)
      },
      () => {
        setLocation(null)
        setIsLoading(false)
      }
    )
  }

  return { location, requestLocation, isLoading }
}

export default useUserLocation
