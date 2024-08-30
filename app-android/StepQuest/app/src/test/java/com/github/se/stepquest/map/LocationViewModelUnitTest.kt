package com.github.se.stepquest.map

import android.location.Location
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Test

class LocationViewModelUnitTest {

  @Test fun startLocationUpdates() {}

  @Test fun appendCurrentLocationToAllocations() {}

  @Test fun getAllocations() {}

  @Test
  fun testcalculateDistance() {
    // Define two location details
    val location1 = LocationDetails(40.7128, -74.0060) // New York
    val location2 = LocationDetails(34.0522, -118.2437) // Los Angeles

    print("Location 1: ${location1.latitude}, ${location1.longitude}")

    // Define the expected distance (approximate value in kilometers)
    val expectedDistanceKm = 3939f
    mockkStatic(Location::class)
    // Mock the result of the distanceBetween method
    every { Location.distanceBetween(any(), any(), any(), any(), any<FloatArray>()) } answers
        {
          val resultArray: FloatArray = arg(4)
          // Mock the result as needed, for example:
          resultArray[0] = 3939.0f
          Unit
        }
    // Call the calculateDistance function
    val actualDistance = calculateDistance(location1, location2)

    // Verify that the distance is calculated correctly (with a tolerance of 1 km)
    assertEquals(expectedDistanceKm, actualDistance, 1f)
  }
}
