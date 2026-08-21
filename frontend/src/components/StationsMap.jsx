import { useEffect, useRef } from 'react'
import { MapContainer, TileLayer, CircleMarker, Popup, useMap } from 'react-leaflet'

// Lives inside <MapContainer> so it can call useMap() -- opens the popup
// for whichever station was just clicked in the results list, and pans
// the map to it.
function MapFocus({ selectedStationId, markerRefs }) {
  const map = useMap()

  useEffect(() => {
    if (!selectedStationId) {
      return
    }

    const marker = markerRefs.current[selectedStationId]

    if (marker) {
      marker.openPopup()
      map.panTo(marker.getLatLng())
    }
  }, [selectedStationId, map, markerRefs])

  return null
}

const BEST_VALUE_COLOR = '#7c3aed'
const BEST_PRICE_COLOR = '#15803d'
const OTHER_STATION_COLOR = '#6b7280'

function StationsMap({
  results,
  userLocation,
  selectedStationId,
  cheapestStationIds,
  bestValueStationIds
}) {
  const markerRefs = useRef({})

  const stationsWithCoords = results.filter(
    (result) => result.latitude != null && result.longitude != null
  )

  if (stationsWithCoords.length === 0) {
    return null
  }

  const center = userLocation
    ? [userLocation.latitude, userLocation.longitude]
    : [stationsWithCoords[0].latitude, stationsWithCoords[0].longitude]

  return (
    <div className="stations-map">
      <MapContainer key={`${center[0]}-${center[1]}`} center={center} zoom={13} scrollWheelZoom={false}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {userLocation && (
          <CircleMarker
            center={[userLocation.latitude, userLocation.longitude]}
            radius={8}
            pathOptions={{ color: '#1d4ed8', fillColor: '#1d4ed8', fillOpacity: 1 }}
          >
            <Popup>Your location</Popup>
          </CircleMarker>
        )}

        {stationsWithCoords.map((result) => {
          // Best Value (purple) takes priority when it's active, since
          // that's the more personalized recommendation; otherwise fall
          // back to Best Price (green); everything else stays neutral.
          const isBestValue = bestValueStationIds?.has(result.stationId)
          const isBestPrice = cheapestStationIds?.has(result.stationId)
          const markerColor = isBestValue
            ? BEST_VALUE_COLOR
            : isBestPrice
              ? BEST_PRICE_COLOR
              : OTHER_STATION_COLOR

          return (
            <CircleMarker
              key={result.stationId}
              ref={(marker) => {
                markerRefs.current[result.stationId] = marker
              }}
              center={[result.latitude, result.longitude]}
              radius={7}
              pathOptions={{ color: markerColor, fillColor: markerColor, fillOpacity: 0.85 }}
            >
              <Popup>
                <strong>{result.stationName}</strong>
                <br />
                {result.price === null ? 'No price reported' : `$${result.price.toFixed(2)}`}
              </Popup>
            </CircleMarker>
          )
        })}

        <MapFocus selectedStationId={selectedStationId} markerRefs={markerRefs} />
      </MapContainer>
    </div>
  )
}

export default StationsMap
