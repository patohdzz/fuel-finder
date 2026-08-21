const EARTH_RADIUS_MILES = 3958.8
const AVERAGE_MPG = 25

function toRadians(degrees) {
  return (degrees * Math.PI) / 180
}

// Straight-line ("as the crow flies") distance -- not real driving
// distance, which would require a routing API.
export function milesBetween(lat1, lon1, lat2, lon2) {
  const dLat = toRadians(lat2 - lat1)
  const dLon = toRadians(lon2 - lon1)

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLon / 2) ** 2

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

  return EARTH_RADIUS_MILES * c
}

// Estimated round-trip fuel cost to drive to a station and back,
// assuming an average 25 MPG vehicle.
export function estimateRoundTripCost(distanceMiles, pricePerGallon) {
  const roundTripGallons = (distanceMiles * 2) / AVERAGE_MPG
  return roundTripGallons * pricePerGallon
}

// Spreads the round-trip drive cost across however many gallons the user
// plans to buy, so it can be added to the pump price as a $/gallon
// surcharge -- keeps the comparison in the same units (price per gallon)
// instead of mixing a lump-sum drive cost into a per-gallon price.
export function estimateValuePricePerGallon(pricePerGallon, roundTripCost, gallonsToBuy) {
  if (!gallonsToBuy || gallonsToBuy <= 0) {
    return pricePerGallon
  }

  return pricePerGallon + roundTripCost / gallonsToBuy
}
