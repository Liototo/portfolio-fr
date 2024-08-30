package com.github.se.stepquest.map

fun compareCheckpoints(
    referenceLocation: LocationDetails,
    newImageLocation: LocationDetails,
    threshold: Float = 10f
): Float {
  val distance = calculateDistance(referenceLocation, newImageLocation)
  if (distance >= threshold) {
    return -1f
  }
  return distance
}
