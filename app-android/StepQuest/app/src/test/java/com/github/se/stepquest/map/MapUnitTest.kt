package com.github.se.stepquest.map

import junit.framework.TestCase.assertEquals
import org.junit.Test

class MapUnitTest {
  @Test fun map() {}

  @Test fun updateMap() {}

  @Test
  fun toLatLng() {
    // Given a location details with latitude and longitude
    val locationDetails = LocationDetails(latitude = 40.7128, longitude = -74.0060)

    // When converting it to LatLng
    val latLng = locationDetails.toLatLng()

    // Then the resulting LatLng should have the same latitude and longitude
    assertEquals(40.7128, latLng.latitude, 0.0)
    assertEquals(-74.0060, latLng.longitude, 0.0)
  }

  @Test fun initMap() {}
}
